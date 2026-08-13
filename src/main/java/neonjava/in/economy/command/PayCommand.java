package neonjava.in.economy.command;

import neonjava.in.economy.Economy;
import neonjava.in.economy.model.Transaction;
import neonjava.in.economy.model.UserProfile;
import neonjava.in.economy.util.ThemeManager;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class PayCommand implements CommandExecutor, TabCompleter {

    private final Economy plugin;

    public PayCommand(Economy plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        ThemeManager tm = plugin.getThemeManager();

        if (!(sender instanceof Player)) {
            sender.sendMessage(tm.formatMessage("&cOnly players can transfer money using /pay."));
            return true;
        }

        Player senderPlayer = (Player) sender;
        if (args.length < 2) {
            senderPlayer.sendMessage(tm.formatMessage("&cUsage: /pay <player> <amount>"));
            return true;
        }

        String targetName = args[0];
        if (targetName.equalsIgnoreCase(senderPlayer.getName())) {
            senderPlayer.sendMessage(tm.formatMessage("&cYou cannot send money to yourself!"));
            return true;
        }

        double amount;
        try {
            amount = Double.parseDouble(args[1]);
            if (amount <= 0 || Double.isNaN(amount) || Double.isInfinite(amount)) {
                senderPlayer.sendMessage(tm.formatMessage("&cPlease enter a valid positive payment amount."));
                return true;
            }
        } catch (NumberFormatException e) {
            senderPlayer.sendMessage(tm.formatMessage("&cInvalid money amount: " + args[1]));
            return true;
        }

        UserProfile senderProfile = plugin.getProfileManager().getOrCreateProfile(senderPlayer);

        if (senderProfile.isLocked()) {
            senderPlayer.sendMessage(tm.formatMessage("&cYour economy account is frozen/locked by an administrator. You cannot transact."));
            senderPlayer.playSound(senderPlayer.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            return true;
        }

        if (senderProfile.getBalance() < amount) {
            senderPlayer.sendMessage(tm.formatMessage("&cInsufficient funds! You have &e"
                    + plugin.getProfileManager().formatCurrency(senderProfile.getBalance())
                    + " &cbut need &e" + plugin.getProfileManager().formatCurrency(amount) + "&c."));
            senderPlayer.playSound(senderPlayer.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            return true;
        }

        UserProfile targetProfile = plugin.getProfileManager().getProfileByName(targetName);
        if (targetProfile == null) {
            Player targetPlayer = Bukkit.getPlayer(targetName);
            if (targetPlayer != null) {
                targetProfile = plugin.getProfileManager().getOrCreateProfile(targetPlayer);
            } else {
                senderPlayer.sendMessage(tm.formatMessage("&cPlayer '&d" + targetName + "&c' does not have an economy profile."));
                return true;
            }
        }

        if (targetProfile.isLocked()) {
            senderPlayer.sendMessage(tm.formatMessage("&cTarget player's account is locked and cannot receive funds."));
            return true;
        }

        // Execute transaction
        senderProfile.withdraw(amount, Transaction.Type.TRANSFER_SENT, "Payment to " + targetProfile.getName(), targetProfile.getName());
        targetProfile.deposit(amount, Transaction.Type.TRANSFER_RECEIVED, "Payment from " + senderPlayer.getName(), senderPlayer.getName());

        plugin.getProfileManager().saveProfiles();

        String formattedAmount = plugin.getProfileManager().formatCurrency(amount);

        senderPlayer.sendMessage(tm.formatMessage("&aSuccessfully sent &e" + formattedAmount + " &ato &d" + targetProfile.getName() + "&a!"));
        senderPlayer.playSound(senderPlayer.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.2f);

        Player onlineTarget = Bukkit.getPlayer(targetProfile.getUuid());
        if (onlineTarget != null && onlineTarget.isOnline()) {
            onlineTarget.sendMessage(tm.formatMessage("&aYou received &e" + formattedAmount + " &afrom &d" + senderPlayer.getName() + "&a!"));
            onlineTarget.playSound(onlineTarget.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.8f, 1.5f);
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            String input = args[0].toLowerCase();
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> !name.equalsIgnoreCase(sender.getName()) && name.toLowerCase().startsWith(input))
                    .collect(Collectors.toList());
        } else if (args.length == 2) {
            List<String> suggestions = new ArrayList<>();
            suggestions.add("100");
            suggestions.add("500");
            suggestions.add("1000");
            suggestions.add("5000");
            return suggestions;
        }
        return new ArrayList<>();
    }
}
