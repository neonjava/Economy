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

public class ProfileCommand implements CommandExecutor, TabCompleter {

    private final Economy plugin;

    public ProfileCommand(Economy plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        ThemeManager tm = plugin.getThemeManager();

        if (!(sender instanceof Player)) {
            sender.sendMessage(tm.formatMessage("&cThis command can only be executed by players."));
            return true;
        }

        Player player = (Player) sender;
        String targetName = args.length > 0 ? args[0] : player.getName();

        UserProfile targetProfile = plugin.getProfileManager().getProfileByName(targetName);
        if (targetProfile == null) {
            Player targetPlayer = Bukkit.getPlayer(targetName);
            if (targetPlayer != null) {
                targetProfile = plugin.getProfileManager().getOrCreateProfile(targetPlayer);
            } else {
                player.sendMessage(tm.formatMessage("&cProfile not found for '&d" + targetName + "&c'."));
                return true;
            }
        }

        plugin.getProfileGUI().openProfileGUI(player, targetProfile);
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
