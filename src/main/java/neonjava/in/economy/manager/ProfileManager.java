package neonjava.in.economy.manager;

import neonjava.in.economy.Economy;
import neonjava.in.economy.model.Transaction;
import neonjava.in.economy.model.UserProfile;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

public class ProfileManager {

    private final Economy plugin;
    private final Map<UUID, UserProfile> profiles;
    private final File dataFile;
    private YamlConfiguration dataConfig;
    private final DecimalFormat currencyFormatter = new DecimalFormat("$#,##0.00");

    public ProfileManager(Economy plugin) {
        this.plugin = plugin;
        this.profiles = new ConcurrentHashMap<>();
        this.dataFile = new File(plugin.getDataFolder(), "profiles.yml");
        loadProfiles();
    }

    public double getInitialBalance() {
        return plugin.getConfig().getDouble("economy.starting-balance", 1000.0);
    }

    public String getCurrencySymbol() {
        return plugin.getConfig().getString("economy.currency-symbol", "$");
    }

    public String formatCurrency(double amount) {
        return currencyFormatter.format(amount);
    }

    public UserProfile getProfile(UUID uuid) {
        return profiles.get(uuid);
    }

    public UserProfile getProfileByName(String name) {
        if (name == null || name.isEmpty()) return null;
        for (UserProfile profile : profiles.values()) {
            if (profile.getName() != null && profile.getName().equalsIgnoreCase(name)) {
                return profile;
            }
        }
        return null;
    }

    public UserProfile getOrCreateProfile(Player player) {
        return getOrCreateProfile(player.getUniqueId(), player.getName());
    }

    public UserProfile getOrCreateProfile(UUID uuid, String name) {
        return profiles.computeIfAbsent(uuid, k -> {
            UserProfile newProfile = new UserProfile(uuid, name, getInitialBalance());
            plugin.getLogger().info("Created new economy profile for " + name + " (" + uuid + ")");
            return newProfile;
        });
    }

    public Collection<UserProfile> getAllProfiles() {
        return Collections.unmodifiableCollection(profiles.values());
    }

    public void applyInterestToAllProfiles() {
        boolean bankEnabled = plugin.getConfig().getBoolean("bank.enabled", true);
        if (!bankEnabled) return;

        double ratePercent = plugin.getConfig().getDouble("bank.interest-rate-percent", 1.5);
        if (ratePercent <= 0) return;

        int paidCount = 0;
        for (UserProfile profile : profiles.values()) {
            if (profile.getBankBalance() > 0) {
                profile.addInterest(ratePercent);
                paidCount++;
            }
        }
        saveProfiles();
        plugin.getLogger().info("Paid " + ratePercent + "% daily savings interest to " + paidCount + " player bank profiles.");
    }

    public synchronized void loadProfiles() {
        profiles.clear();
        if (!dataFile.exists()) {
            plugin.saveResource("profiles.yml", false);
        }

        dataConfig = YamlConfiguration.loadConfiguration(dataFile);
        ConfigurationSection section = dataConfig.getConfigurationSection("profiles");
        if (section == null) return;

        for (String uuidStr : section.getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(uuidStr);
                ConfigurationSection pSec = section.getConfigurationSection(uuidStr);
                if (pSec == null) continue;

                String name = pSec.getString("name", "Unknown");
                double balance = pSec.getDouble("balance", getInitialBalance());
                double bankBalance = pSec.getDouble("bankBalance", 0.0);
                double totalEarned = pSec.getDouble("totalEarned", balance);
                double totalSpent = pSec.getDouble("totalSpent", 0.0);
                long firstJoin = pSec.getLong("firstJoin", System.currentTimeMillis());
                long lastSeen = pSec.getLong("lastSeen", System.currentTimeMillis());
                boolean locked = pSec.getBoolean("locked", false);

                List<Transaction> transactions = new ArrayList<>();
                ConfigurationSection txSec = pSec.getConfigurationSection("transactions");
                if (txSec != null) {
                    for (String key : txSec.getKeys(false)) {
                        ConfigurationSection t = txSec.getConfigurationSection(key);
                        if (t == null) continue;

                        long ts = t.getLong("timestamp", System.currentTimeMillis());
                        String typeName = t.getString("type", Transaction.Type.DEPOSIT.name());
                        Transaction.Type type;
                        try {
                            type = Transaction.Type.valueOf(typeName);
                        } catch (IllegalArgumentException e) {
                            type = Transaction.Type.DEPOSIT;
                        }
                        double amt = t.getDouble("amount", 0.0);
                        String desc = t.getString("description", "");
                        String rel = t.getString("relatedPlayer", "System");

                        transactions.add(new Transaction(ts, type, amt, desc, rel));
                    }
                }

                UserProfile profile = new UserProfile(uuid, name, balance, bankBalance, totalEarned, totalSpent, firstJoin, lastSeen, locked, transactions);
                profiles.put(uuid, profile);
            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to load profile for UUID: " + uuidStr, e);
            }
        }
        plugin.getLogger().info("Loaded " + profiles.size() + " user profiles.");
    }

    public synchronized void saveProfiles() {
        dataConfig = new YamlConfiguration();
        ConfigurationSection section = dataConfig.createSection("profiles");

        for (UserProfile profile : profiles.values()) {
            ConfigurationSection pSec = section.createSection(profile.getUuid().toString());
            pSec.set("name", profile.getName());
            pSec.set("balance", profile.getBalance());
            pSec.set("bankBalance", profile.getBankBalance());
            pSec.set("totalEarned", profile.getTotalEarned());
            pSec.set("totalSpent", profile.getTotalSpent());
            pSec.set("firstJoin", profile.getFirstJoin());
            pSec.set("lastSeen", profile.getLastSeen());
            pSec.set("locked", profile.isLocked());

            ConfigurationSection txSec = pSec.createSection("transactions");
            List<Transaction> txList = profile.getTransactions();
            for (int i = 0; i < txList.size(); i++) {
                Transaction t = txList.get(i);
                ConfigurationSection tSec = txSec.createSection(String.valueOf(i));
                tSec.set("timestamp", t.getTimestamp());
                tSec.set("type", t.getType().name());
                tSec.set("amount", t.getAmount());
                tSec.set("description", t.getDescription());
                tSec.set("relatedPlayer", t.getRelatedPlayer());
            }
        }

        try {
            dataConfig.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not save profiles.yml", e);
        }
    }
}
