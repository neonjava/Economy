package neonjava.in.economy.command;

import neonjava.in.economy.Economy;
import neonjava.in.economy.model.Transaction;
import neonjava.in.economy.model.UserProfile;
import neonjava.in.economy.util.ThemeManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class EconomyCommand implements CommandExecutor, TabCompleter {

    private final Economy plugin;

    public EconomyCommand(Economy plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        ThemeManager tm = plugin.getThemeManager();

        if (!sender.hasPermission("economy.admin")) {
            sender.sendMessage(tm.formatMessage("&cYou do not have permission to execute admin economy commands."));
            return true;
        }

        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        String sub = args[0].toLowerCase();

        if (sub.equals("reload")) {
            plugin.reloadConfig();
            plugin.getThemeManager().reloadTheme();
            plugin.getShopManager().loadShop();
            plugin.getProfileManager().loadProfiles();
            sender.sendMessage(tm.formatMessage("&aConfiguration, color theme, shop categories, and player profiles reloaded successfully!"));
            return true;
        }

        if (sub.equals("save")) {
            plugin.getProfileManager().saveProfiles();
            sender.sendMessage(tm.formatMessage("&aAll economy player profiles saved to disk."));
            return true;
        }

        if (args.length < 2) {
            sendHelp(sender);
            return true;
        }

        String targetName = args[1];
        UserProfile profile = plugin.getProfileManager().getProfileByName(targetName);
        if (profile == null) {
            Player onlinePlayer = Bukkit.getPlayer(targetName);
            if (onlinePlayer != null) {
                profile = plugin.getProfileManager().getOrCreateProfile(onlinePlayer);
            } else {
                sender.sendMessage(tm.formatMessage("&cNo economy profile found for player '&d" + targetName + "&c'."));
                return true;
            }
        }

        if (sub.equals("reset")) {
            double starting = plugin.getProfileManager().getInitialBalance();
            profile.setBalance(starting);
            profile.addTransaction(new Transaction(Transaction.Type.ADMIN_SET, starting, "Reset by Admin " + sender.getName(), sender.getName()));
            plugin.getProfileManager().saveProfiles();
            sender.sendMessage(tm.formatMessage("&aReset &d" + profile.getName() + "&a's profile balance to &e" + plugin.getProfileManager().formatCurrency(starting)));
            return true;
        }

        if (args.length < 3) {
            sender.sendMessage(tm.formatMessage("&cUsage: /eco " + sub + " <player> <amount>"));
            return true;
        }

        double amount;
        try {
            amount = Double.parseDouble(args[2]);
            if (amount < 0 || Double.isNaN(amount) || Double.isInfinite(amount)) {
                sender.sendMessage(tm.formatMessage("&cAmount must be a non-negative number."));
                return true;
            }
        } catch (NumberFormatException e) {
            sender.sendMessage(tm.formatMessage("&cInvalid numeric amount: " + args[2]));
            return true;
        }

        switch (sub) {
            case "give":
            case "add":
                profile.deposit(amount, Transaction.Type.ADMIN_ADD, "Given by Admin " + sender.getName(), sender.getName());
                sender.sendMessage(tm.formatMessage("&aGave &e" + plugin.getProfileManager().formatCurrency(amount) + " &ato &d" + profile.getName() + "&a. New Balance: &e" + plugin.getProfileManager().formatCurrency(profile.getBalance())));
                break;

            case "take":
            case "remove":
                profile.withdraw(amount, Transaction.Type.ADMIN_TAKE, "Deducted by Admin " + sender.getName(), sender.getName());
                sender.sendMessage(tm.formatMessage("&cTook &e" + plugin.getProfileManager().formatCurrency(amount) + " &cfrom &d" + profile.getName() + "&c. New Balance: &e" + plugin.getProfileManager().formatCurrency(profile.getBalance())));
                break;

            case "set":
                double oldBal = profile.getBalance();
                profile.setBalance(amount);
                profile.addTransaction(new Transaction(Transaction.Type.ADMIN_SET, amount, "Set by Admin " + sender.getName() + " (was " + oldBal + ")", sender.getName()));
                sender.sendMessage(tm.formatMessage("&aSet &d" + profile.getName() + "&a's balance to &e" + plugin.getProfileManager().formatCurrency(amount)));
                break;

            default:
                sendHelp(sender);
                return true;
        }

        plugin.getProfileManager().saveProfiles();
        return true;
    }

    private void sendHelp(CommandSender sender) {
        ThemeManager tm = plugin.getThemeManager();
        sender.sendMessage(tm.formatHeader("Economy Admin"));
        sender.sendMessage(tm.getPrimary() + "/eco give <player> <amount> " + tm.getNeutral() + "- Give money to a player profile");
        sender.sendMessage(tm.getPrimary() + "/eco take <player> <amount> " + tm.getNeutral() + "- Take money from a player profile");
        sender.sendMessage(tm.getPrimary() + "/eco set <player> <amount>  " + tm.getNeutral() + "- Set player profile balance");
        sender.sendMessage(tm.getPrimary() + "/eco reset <player>         " + tm.getNeutral() + "- Reset player balance to default");
        sender.sendMessage(tm.getPrimary() + "/eco save                   " + tm.getNeutral() + "- Force save all player profiles");
        sender.sendMessage(tm.getPrimary() + "/eco reload                 " + tm.getNeutral() + "- Reload configuration & theme");
        sender.sendMessage(tm.getNeutral() + "=================================================");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("economy.admin")) return new ArrayList<>();

        if (args.length == 1) {
            List<String> subs = Arrays.asList("give", "take", "set", "reset", "save", "reload");
            String input = args[0].toLowerCase();
            return subs.stream().filter(s -> s.startsWith(input)).collect(Collectors.toList());
        } else if (args.length == 2 && !args[0].equalsIgnoreCase("save") && !args[0].equalsIgnoreCase("reload")) {
            String input = args[1].toLowerCase();
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(input))
                    .collect(Collectors.toList());
        } else if (args.length == 3 && (args[0].equalsIgnoreCase("give") || args[0].equalsIgnoreCase("take") || args[0].equalsIgnoreCase("set"))) {
            return Arrays.asList("100", "500", "1000", "5000", "10000");
        }
        return new ArrayList<>();
    }
}
