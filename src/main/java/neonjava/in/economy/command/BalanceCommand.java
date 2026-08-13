package neonjava.in.economy.command;

import neonjava.in.economy.Economy;
import neonjava.in.economy.model.UserProfile;
import neonjava.in.economy.util.ThemeManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class BalanceCommand implements CommandExecutor, TabCompleter {

    private final Economy plugin;

    public BalanceCommand(Economy plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        ThemeManager tm = plugin.getThemeManager();
        String targetName;

        if (args.length > 0) {
            targetName = args[0];
        } else if (sender instanceof Player) {
            targetName = sender.getName();
        } else {
            sender.sendMessage(tm.formatMessage("&cConsole must specify a player username: /balance <player>"));
            return true;
        }

        UserProfile profile = plugin.getProfileManager().getProfileByName(targetName);
        if (profile == null) {
            Player targetPlayer = Bukkit.getPlayer(targetName);
            if (targetPlayer != null) {
                profile = plugin.getProfileManager().getOrCreateProfile(targetPlayer);
            } else {
                sender.sendMessage(tm.formatMessage("&cNo economy profile found for player '&d" + targetName + "&c'."));
                return true;
            }
        }

        String formattedBal = plugin.getProfileManager().formatCurrency(profile.getBalance());
        if (sender instanceof Player && ((Player) sender).getName().equalsIgnoreCase(profile.getName())) {
            sender.sendMessage(tm.formatMessage("&fYour current balance: " + tm.getSuccess() + formattedBal));
        } else {
            sender.sendMessage(tm.formatMessage("&fBalance of " + tm.getPrimary() + profile.getName() + "&f: " + tm.getSuccess() + formattedBal));
        }

        if (sender instanceof Player) {
            sender.sendMessage(tm.formatMessage("&7Tip: Use &d/profilemanage " + profile.getName() + " &7to view profile GUI."));
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            String input = args[0].toLowerCase();
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(input))
                    .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }
}
