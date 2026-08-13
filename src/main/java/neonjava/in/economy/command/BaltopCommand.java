package neonjava.in.economy.command;

import neonjava.in.economy.Economy;
import neonjava.in.economy.model.UserProfile;
import neonjava.in.economy.util.ThemeManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class BaltopCommand implements CommandExecutor, TabCompleter {

    private final Economy plugin;

    public BaltopCommand(Economy plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        ThemeManager tm = plugin.getThemeManager();

        if (sender instanceof Player && args.length == 0) {
            plugin.getProfileGUI().openBaltopGUI((Player) sender);
            return true;
        }

        List<UserProfile> sortedProfiles = new ArrayList<>(plugin.getProfileManager().getAllProfiles());
        sortedProfiles.sort((p1, p2) -> Double.compare(p2.getBalance(), p1.getBalance()));

        sender.sendMessage(tm.formatHeader("Top Balances"));
        int limit = Math.min(10, sortedProfiles.size());
        if (limit == 0) {
            sender.sendMessage(tm.getNeutral() + "No economy profiles recorded yet.");
        } else {
            for (int i = 0; i < limit; i++) {
                UserProfile p = sortedProfiles.get(i);
                int rank = i + 1;
                String medal = rank == 1 ? "🥇 " : (rank == 2 ? "🥈 " : (rank == 3 ? "🥉 " : ""));
                sender.sendMessage(tm.getSecondary() + "#" + rank + " " + medal + tm.getPrimary() + p.getName() + tm.getAccent() + ": " + tm.getSuccess() + plugin.getProfileManager().formatCurrency(p.getBalance()));
            }
        }
        sender.sendMessage(tm.getNeutral() + "=================================================");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return new ArrayList<>();
    }
}
