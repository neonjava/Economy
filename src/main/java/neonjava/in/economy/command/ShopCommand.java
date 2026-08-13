package neonjava.in.economy.command;

import neonjava.in.economy.Economy;
import neonjava.in.economy.util.ThemeManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class ShopCommand implements CommandExecutor, TabCompleter {

    private final Economy plugin;

    public ShopCommand(Economy plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        ThemeManager tm = plugin.getThemeManager();

        if (!(sender instanceof Player)) {
            sender.sendMessage(tm.formatMessage("&cOnly players can open the GUI Shop in-game."));
            return true;
        }

        Player player = (Player) sender;
        plugin.getProfileGUI().openShopCategoriesGUI(player);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return new ArrayList<>();
    }
}
