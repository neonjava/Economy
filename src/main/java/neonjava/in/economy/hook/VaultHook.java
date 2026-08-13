package neonjava.in.economy.hook;

import neonjava.in.economy.Economy;
import neonjava.in.economy.model.Transaction;
import neonjava.in.economy.model.UserProfile;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.util.Collections;
import java.util.List;

public class VaultHook implements net.milkbowl.vault.economy.Economy {

    private final Economy plugin;

    public VaultHook(Economy plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean isEnabled() {
        return plugin.isEnabled();
    }

    @Override
    public String getName() {
        return "Economy";
    }

    @Override
    public boolean hasBankSupport() {
        return false;
    }

    @Override
    public int fractionalDigits() {
        return 2;
    }

    @Override
    public String format(double amount) {
        return plugin.getProfileManager().formatCurrency(amount);
    }

    @Override
    public String currencyNamePlural() {
        return plugin.getConfig().getString("economy.currency-name", "Dollars");
    }

    @Override
    public String currencyNameSingular() {
        return plugin.getConfig().getString("economy.currency-name", "Dollar");
    }

    @Override
    public boolean hasAccount(String playerName) {
        return plugin.getProfileManager().getProfileByName(playerName) != null;
    }

    @Override
    public boolean hasAccount(OfflinePlayer player) {
        if (player == null) return false;
        return plugin.getProfileManager().getProfile(player.getUniqueId()) != null ||
                (player.getName() != null && plugin.getProfileManager().getProfileByName(player.getName()) != null);
    }

    @Override
    public boolean hasAccount(String playerName, String worldName) {
        return hasAccount(playerName);
    }

    @Override
    public boolean hasAccount(OfflinePlayer player, String worldName) {
        return hasAccount(player);
    }

    @Override
    public double getBalance(String playerName) {
        UserProfile profile = plugin.getProfileManager().getProfileByName(playerName);
        return profile != null ? profile.getBalance() : 0.0;
    }

    @Override
    public double getBalance(OfflinePlayer player) {
        if (player == null) return 0.0;
        UserProfile profile = plugin.getProfileManager().getProfile(player.getUniqueId());
        if (profile == null && player.getName() != null) {
            profile = plugin.getProfileManager().getProfileByName(player.getName());
        }
        return profile != null ? profile.getBalance() : 0.0;
    }

    @Override
    public double getBalance(String playerName, String world) {
        return getBalance(playerName);
    }

    @Override
    public double getBalance(OfflinePlayer player, String world) {
        return getBalance(player);
    }

    @Override
    public boolean has(String playerName, double amount) {
        return getBalance(playerName) >= amount;
    }

    @Override
    public boolean has(OfflinePlayer player, double amount) {
        return getBalance(player) >= amount;
    }

    @Override
    public boolean has(String playerName, String worldName, double amount) {
        return has(playerName, amount);
    }

    @Override
    public boolean has(OfflinePlayer player, String worldName, double amount) {
        return has(player, amount);
    }

    @Override
    public EconomyResponse withdrawPlayer(String playerName, double amount) {
        if (amount < 0) {
            return new EconomyResponse(0, getBalance(playerName), EconomyResponse.ResponseType.FAILURE, "Cannot withdraw negative amount");
        }
        UserProfile profile = plugin.getProfileManager().getProfileByName(playerName);
        if (profile == null) {
            return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, "User profile not found");
        }
        if (profile.isLocked()) {
            return new EconomyResponse(0, profile.getBalance(), EconomyResponse.ResponseType.FAILURE, "Account is locked");
        }
        if (profile.getBalance() < amount) {
            return new EconomyResponse(0, profile.getBalance(), EconomyResponse.ResponseType.FAILURE, "Insufficient funds");
        }

        profile.withdraw(amount, Transaction.Type.WITHDRAW, "Vault API Withdrawal", "Vault");
        plugin.getProfileManager().saveProfiles();
        return new EconomyResponse(amount, profile.getBalance(), EconomyResponse.ResponseType.SUCCESS, null);
    }

    @Override
    public EconomyResponse withdrawPlayer(OfflinePlayer player, double amount) {
        if (player == null || player.getName() == null) {
            return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, "Invalid player");
        }
        UserProfile profile = plugin.getProfileManager().getProfile(player.getUniqueId());
        if (profile == null) {
            profile = plugin.getProfileManager().getProfileByName(player.getName());
        }
        if (profile == null) {
            return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, "Profile not found");
        }
        return withdrawPlayer(profile.getName(), amount);
    }

    @Override
    public EconomyResponse withdrawPlayer(String playerName, String worldName, double amount) {
        return withdrawPlayer(playerName, amount);
    }

    @Override
    public EconomyResponse withdrawPlayer(OfflinePlayer player, String worldName, double amount) {
        return withdrawPlayer(player, amount);
    }

    @Override
    public EconomyResponse depositPlayer(String playerName, double amount) {
        if (amount < 0) {
            return new EconomyResponse(0, getBalance(playerName), EconomyResponse.ResponseType.FAILURE, "Cannot deposit negative amount");
        }
        UserProfile profile = plugin.getProfileManager().getProfileByName(playerName);
        if (profile == null) {
            // Auto create profile if online
            OfflinePlayer offPlayer = Bukkit.getOfflinePlayer(playerName);
            if (offPlayer.getUniqueId() != null) {
                profile = plugin.getProfileManager().getOrCreateProfile(offPlayer.getUniqueId(), playerName);
            } else {
                return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, "User profile not found");
            }
        }
        if (profile.isLocked()) {
            return new EconomyResponse(0, profile.getBalance(), EconomyResponse.ResponseType.FAILURE, "Account is locked");
        }

        profile.deposit(amount, Transaction.Type.DEPOSIT, "Vault API Deposit", "Vault");
        plugin.getProfileManager().saveProfiles();
        return new EconomyResponse(amount, profile.getBalance(), EconomyResponse.ResponseType.SUCCESS, null);
    }

    @Override
    public EconomyResponse depositPlayer(OfflinePlayer player, double amount) {
        if (player == null) {
            return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, "Invalid player");
        }
        UserProfile profile = plugin.getProfileManager().getProfile(player.getUniqueId());
        if (profile == null && player.getName() != null) {
            profile = plugin.getProfileManager().getOrCreateProfile(player.getUniqueId(), player.getName());
        }
        if (profile == null) {
            return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, "Profile not found");
        }
        return depositPlayer(profile.getName(), amount);
    }

    @Override
    public EconomyResponse depositPlayer(String playerName, String worldName, double amount) {
        return depositPlayer(playerName, amount);
    }

    @Override
    public EconomyResponse depositPlayer(OfflinePlayer player, String worldName, double amount) {
        return depositPlayer(player, amount);
    }

    @Override
    public boolean createPlayerAccount(String playerName) {
        if (hasAccount(playerName)) return false;
        OfflinePlayer offPlayer = Bukkit.getOfflinePlayer(playerName);
        plugin.getProfileManager().getOrCreateProfile(offPlayer.getUniqueId(), playerName);
        plugin.getProfileManager().saveProfiles();
        return true;
    }

    @Override
    public boolean createPlayerAccount(OfflinePlayer player) {
        if (hasAccount(player)) return false;
        plugin.getProfileManager().getOrCreateProfile(player.getUniqueId(), player.getName() != null ? player.getName() : "Unknown");
        plugin.getProfileManager().saveProfiles();
        return true;
    }

    @Override
    public boolean createPlayerAccount(String playerName, String worldName) {
        return createPlayerAccount(playerName);
    }

    @Override
    public boolean createPlayerAccount(OfflinePlayer player, String worldName) {
        return createPlayerAccount(player);
    }

    // --- Unimplemented Bank Methods ---
    @Override
    public EconomyResponse createBank(String name, String player) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Banks are not supported.");
    }

    @Override
    public EconomyResponse createBank(String name, OfflinePlayer player) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Banks are not supported.");
    }

    @Override
    public EconomyResponse deleteBank(String name) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Banks are not supported.");
    }

    @Override
    public EconomyResponse bankBalance(String name) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Banks are not supported.");
    }

    @Override
    public EconomyResponse bankHas(String name, double amount) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Banks are not supported.");
    }

    @Override
    public EconomyResponse bankWithdraw(String name, double amount) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Banks are not supported.");
    }

    @Override
    public EconomyResponse bankDeposit(String name, double amount) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Banks are not supported.");
    }

    @Override
    public EconomyResponse isBankOwner(String name, String playerName) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Banks are not supported.");
    }

    @Override
    public EconomyResponse isBankOwner(String name, OfflinePlayer player) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Banks are not supported.");
    }

    @Override
    public EconomyResponse isBankMember(String name, String playerName) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Banks are not supported.");
    }

    @Override
    public EconomyResponse isBankMember(String name, OfflinePlayer player) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Banks are not supported.");
    }

    @Override
    public List<String> getBanks() {
        return Collections.emptyList();
    }
}
