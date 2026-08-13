package neonjava.in.economy.hook;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import neonjava.in.economy.Economy;
import neonjava.in.economy.model.UserProfile;
import org.bukkit.OfflinePlayer;

import java.util.ArrayList;
import java.util.List;

public class PAPIExpansion extends PlaceholderExpansion {

    private final Economy plugin;

    public PAPIExpansion(Economy plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getIdentifier() {
        return "economy";
    }

    @Override
    public String getAuthor() {
        return plugin.getDescription().getAuthors().toString();
    }

    @Override
    public String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public boolean canRegister() {
        return true;
    }

    @Override
    public String onRequest(OfflinePlayer player, String params) {
        if (player == null) return "";

        UserProfile profile = plugin.getProfileManager().getProfile(player.getUniqueId());
        if (profile == null && player.getName() != null) {
            profile = plugin.getProfileManager().getProfileByName(player.getName());
        }

        if (profile == null) return "";

        switch (params.toLowerCase()) {
            case "balance":
                return String.valueOf(profile.getBalance());

            case "balance_formatted":
                return plugin.getProfileManager().formatCurrency(profile.getBalance());

            case "bank_balance":
                return String.valueOf(profile.getBankBalance());

            case "bank_balance_formatted":
                return plugin.getProfileManager().formatCurrency(profile.getBankBalance());

            case "total_earned":
                return String.valueOf(profile.getTotalEarned());

            case "total_spent":
                return String.valueOf(profile.getTotalSpent());

            case "status":
                return profile.isLocked() ? "Locked" : "Active";

            case "rank":
                List<UserProfile> sorted = new ArrayList<>(plugin.getProfileManager().getAllProfiles());
                sorted.sort((p1, p2) -> Double.compare(p2.getBalance(), p1.getBalance()));
                for (int i = 0; i < sorted.size(); i++) {
                    if (sorted.get(i).getUuid().equals(profile.getUuid())) {
                        return "#" + (i + 1);
                    }
                }
                return "#N/A";

            default:
                return null;
        }
    }
}
