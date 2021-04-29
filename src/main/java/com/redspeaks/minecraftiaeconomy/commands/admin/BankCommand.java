package com.redspeaks.minecraftiaeconomy.commands.admin;

import com.redspeaks.minecraftiaeconomy.api.*;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Optional;
import java.util.Random;

@CommandInfo(name = "bank", requiresPlayer = false)
public class BankCommand extends AbstractCommand {

    private Bank bank = MinecraftiaEconomyManager.getBank();

    @Override
    public void execute(CommandSender sender, String[] args) {
        String node = "economy.command.bank";
        if(!hasPermissionFrom(sender, node, "info", "create", "delete", "assign")) {
            sendNoPermission(sender);
            return;
        }
        if(args.length < 2) {
            sendCorrectArguments(sender, "bank info <owner / account>", "bank create <owner> <account>", "bank delete <account>", "bank assign <player> <account>",
                    "bank transfer <account> <bank/player> <name> <amount>",
                    "bank set <account> <amount>", "bank give <name> <amount>");
            return;
        }

        String subCommand = args[0];


        if(subCommand.equalsIgnoreCase("set")) {
            if(!sender.hasPermission(node + subCommand.toLowerCase())) {
                sendNoPermission(sender);
                return;
            }
            if(args.length != 3) {
                sendCorrectUsage(sender, "bank set <account> <amount>");
                return;
            }
            String account = args[1];
            if(!ChatUtil.isInt(args[2])) {
                sender.sendMessage(ChatUtil.colorize("&7Please enter numbers only for amount"));
                return;
            }
            double amount = Double.parseDouble(args[2]);
            MinecraftiaEconomyManager.getBank().setBalance(account, amount);
            sendMessage(sender, "&7Bank Set: &aSuccessful");
            sendMessage(sender, "&7Bank: &a" + account);
            sendMessage(sender, "&7New Balance: &a" + amount);
            return;
        }

        if(subCommand.equalsIgnoreCase("give")) {
            if(!sender.hasPermission(node + subCommand.toLowerCase())) {
                sendNoPermission(sender);
                return;
            }
            if(args.length != 3) {
                sendCorrectUsage(sender, "bank give <account> <amount>");
                return;
            }
            String account = args[1];
            if(!ChatUtil.isInt(args[2])) {
                sender.sendMessage(ChatUtil.colorize("&7Please enter numbers only for amount"));
                return;
            }
            double amount = Double.parseDouble(args[2]);
            MinecraftiaEconomyManager.getBank().setBalance(account,
                    MinecraftiaEconomyManager.getBank().getBalance(account).get() + amount);
            sendMessage(sender, "&7Bank Give: &aSuccessful");
            sendMessage(sender, "&7Bank: &a" + account);
            sendMessage(sender, "&7Added: &a" + amount);
            sendMessage(sender, "&7New Balance: &a" + MinecraftiaEconomyManager.getBank().getBalance(account));
            return;
        }

        if(subCommand.equalsIgnoreCase("transfer")) {
            if(!sender.hasPermission(node + subCommand.toLowerCase())) {
                sendNoPermission(sender);
                return;
            }
            if(args.length != 5) {
                sendCorrectUsage(sender, "bank transfer <account> <bank/player> <name>");
                return;
            }
            if(!(sender instanceof Player)) {
                sendMessage(sender, "&7You must be a player to do that");
                return;
            }

            String bank = args[1];
            String recipient = args[2];
            if(!ChatUtil.isInt(args[4])) {
                sender.sendMessage(ChatUtil.colorize("&7Please enter numbers only for amount"));
                return;
            }
            double amount = Double.parseDouble(args[4]);
            if(recipient.equalsIgnoreCase("player")) {
                OfflinePlayer target = Bukkit.getOfflinePlayer(args[3]);
                if(!target.hasPlayedBefore()) {
                    sendMessage(sender, "&7Player &c" + args[1] + " &7never played the server before");
                    return;
                }
                if(MinecraftiaEconomyManager.getBank().transfer(bank, target, amount)) {
                    sendMessage(sender, "&7Bank Transfer: &aSuccessful");
                    sendMessage(sender, "&7From: &b" + bank);
                    sendMessage(sender, "&7To player: &b" + target.getName());
                    sendMessage(sender, "&7Amount: &b" + ChatUtil.commas(amount) + MinecraftiaEconomyManager.getEconomy().getSuffix());
                } else {
                    sendMessage(sender, "&7Bank Transfer: &cUnsuccessful");
                    sendMessage(sender, "&7Possible Errors:");
                    sendMessage(sender, "   &f- &bBank does not exist");
                    sendMessage(sender, "   &f- &bInsufficient balance");
                }
                return;
            }
            if(recipient.equalsIgnoreCase("bank")) {
                if(!MinecraftiaEconomyManager.bankExists(args[3])) {
                    sendMessage(sender, "&7Bank &c" + args[3] + "does not exist");
                    return;
                }
                if(MinecraftiaEconomyManager.getBank().transfer(bank, args[3], amount)) {
                    sendMessage(sender, "&7Bank Transfer: &aSuccessful");
                    sendMessage(sender, "&7From: &b" + bank);
                    sendMessage(sender, "&7To bank: &b" + args[3]);
                    sendMessage(sender, "&7Amount: &b" + ChatUtil.commas(amount) + MinecraftiaEconomyManager.getEconomy().getSuffix());
                } else {
                    sendMessage(sender, "&7Bank Transfer: &cUnsuccessful");
                    sendMessage(sender, "&7Possible Errors:");
                    sendMessage(sender, "   &f- &bBank does not exist");
                    sendMessage(sender, "   &f- &bInsufficient balance");
                }
                return;
            }
            sendCorrectUsage(sender, "bank transfer <account> <bank/player> <name>");
            return;
        }

        if(subCommand.equalsIgnoreCase("create")) {
            if(!sender.hasPermission(node + subCommand.toLowerCase())) {
                sendNoPermission(sender);
                return;
            }
            if(args.length != 3) {
                sendCorrectUsage(sender, "bank create <owner> <name>");
                return;
            }
            OfflinePlayer owner = Bukkit.getOfflinePlayer(args[1]);
            String name = args[2];

            if(!owner.hasPlayedBefore()) {
                sendMessage(sender, "&7Player &c" + args[1] + " &7never played the server before");
                return;
            }

            if(bank.create(name, owner)) {
                sendMessage(sender, "&7Creation: &aSuccessful");
                sendMessage(sender, "&7Bank: &6" + name);
                sendMessage(sender, "&7Assigned to: &6" + owner.getName());

                if(owner.isOnline()) {
                    sendMessage(owner.getPlayer(), "&7Bank: &6" + name + " &7has been assigned to you");
                    Random random = new Random();
                    owner.getPlayer().playSound(owner.getPlayer().getLocation(), Sound.values()[random.nextInt(Sound.values().length)], 1.0f, 1.0f);
                    random = null;
                }
            } else {
                sendMessage(sender, "&7Creation: &cUnsuccessful");
                sendMessage(sender, "&7Possible errors:");
                sendMessage(sender, "  &f- &cBank already exists");
                sendMessage(sender, "  &f- &cTarget player have two bank accounts");
            }
            return;
        }

        if(subCommand.equalsIgnoreCase("assign")) {

            if(!sender.hasPermission(node + subCommand.toLowerCase())) {
                sendNoPermission(sender);
                return;
            }

            if(args.length != 3) {
                sendCorrectUsage(sender, "bank assign <owner> <name>");
                return;
            }
            OfflinePlayer owner = Bukkit.getOfflinePlayer(args[1]);
            String name = args[2];

            if(!owner.hasPlayedBefore()) {
                sendMessage(sender, "&7Player &c" + args[1] + " &7never played the server before");
                return;
            }

            Optional<OfflinePlayer> previousOwner = MinecraftiaEconomyManager.getBankOwner(name);
            if(bank.transferOwnerShip(name, owner)) {
                sendMessage(sender, "&7Assign: &aSuccessful");
                sendMessage(sender, "&7Bank: &6" + name);
                sendMessage(sender, "&7Assigned to: &6" + owner.getName());
                sendMessage(sender, "&7Previous owner: &6" + previousOwner.get().getName());

                if(owner.isOnline()) {
                    sendMessage(owner.getPlayer(), "&7Bank: &6" + name + " &7has been assigned to you");
                    Random random = new Random();
                    owner.getPlayer().playSound(owner.getPlayer().getLocation(), Sound.values()[random.nextInt(Sound.values().length)], 1.0f, 1.0f);
                    random = null;
                }
            } else {
                sendMessage(sender, "&7Assign: &cUnsuccessful");
                sendMessage(sender, "&7Possible errors:");
                sendMessage(sender, "  &f- &cBank does not exist");
                sendMessage(sender, "  &f- &cTarget player have two bank accounts");
            }
            return;
        }

        if(subCommand.equalsIgnoreCase("delete")) {
            if(!sender.hasPermission(node + subCommand.toLowerCase())) {
                sendNoPermission(sender);
                return;
            }
            String name = args[1];
            Optional<OfflinePlayer> owner = MinecraftiaEconomyManager.getBankOwner(name);
            if(bank.delete(name)) {
                if(owner.isPresent() && owner.get().isOnline()) {
                    sendMessage(owner.get().getPlayer(), "&7Bank: &6" + name + " &7has been unassigned to you");
                    Random random = new Random();
                    owner.get().getPlayer().playSound(owner.get().getPlayer().getLocation(), Sound.values()[random.nextInt(Sound.values().length)], 1.0f, 1.0f);
                    random = null;
                }
                sendMessage(sender, "&7Delete: &aSuccessful");
                sendMessage(sender, "&7Bank: &6" + name);
                sendMessage(sender, "&7Assigned to: &6" + owner.get().getName());
            } else {
                sendMessage(sender, "&7Assign: &cUnsuccessful");
                sendMessage(sender, "&7Possible errors:");
                sendMessage(sender, "  &f- &cBank does not exist");
            }
            return;
        }
        sendCorrectArguments(sender, "bank info <owner / name>", "bank create <owner> <name>", "bank delete <name>", "bank assign <player> <name>");
    }
}
