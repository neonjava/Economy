package neonjava.in.economy;

import neonjava.in.economy.command.*;
import neonjava.in.economy.gui.ProfileGUI;
import neonjava.in.economy.hook.PAPIExpansion;
import neonjava.in.economy.hook.VaultHook;
import neonjava.in.economy.listener.ChatInputListener;
import neonjava.in.economy.listener.GUIListener;
import neonjava.in.economy.listener.PlayerJoinListener;
import neonjava.in.economy.manager.ProfileManager;
import neonjava.in.economy.util.ThemeManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class Economy extends JavaPlugin {

    private ProfileManager profileManager;
    private ProfileGUI profileGUI;
    private ThemeManager themeManager;
    private ChatInputListener chatInputListener;

    @Override
    public void onEnable() {
        // Save default config
        saveDefaultConfig();

        // Initialize Theme Manager & Plugin Components
        this.themeManager = new ThemeManager(this);
        this.profileManager = new ProfileManager(this);
        this.profileGUI = new ProfileGUI(this);
        this.chatInputListener = new ChatInputListener(this);

        // Register Event Listeners
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);
        getServer().getPluginManager().registerEvents(new GUIListener(this), this);
        getServer().getPluginManager().registerEvents(chatInputListener, this);

        // Register Commands
        ProfileManageCommand profileManageCmd = new ProfileManageCommand(this);
        getCommand("profilemanage").setExecutor(profileManageCmd);
        getCommand("profilemanage").setTabCompleter(profileManageCmd);

        ProfileCommand profileCmd = new ProfileCommand(this);
        getCommand("profile").setExecutor(profileCmd);
        getCommand("profile").setTabCompleter(profileCmd);

        BalanceCommand balanceCmd = new BalanceCommand(this);
        getCommand("balance").setExecutor(balanceCmd);
        getCommand("balance").setTabCompleter(balanceCmd);

        PayCommand payCmd = new PayCommand(this);
        getCommand("pay").setExecutor(payCmd);
        getCommand("pay").setTabCompleter(payCmd);

        EconomyCommand ecoCmd = new EconomyCommand(this);
        getCommand("eco").setExecutor(ecoCmd);
        getCommand("eco").setTabCompleter(ecoCmd);

        BaltopCommand baltopCmd = new BaltopCommand(this);
        getCommand("baltop").setExecutor(baltopCmd);
        getCommand("baltop").setTabCompleter(baltopCmd);

        // Vault API Hook Registration
        if (Bukkit.getPluginManager().getPlugin("Vault") != null) {
            try {
                Bukkit.getServicesManager().register(
                        net.milkbowl.vault.economy.Economy.class,
                        new VaultHook(this),
                        this,
                        org.bukkit.plugin.ServicePriority.Highest
                );
                getLogger().info("Successfully hooked into Vault API as Economy Provider!");
            } catch (Exception e) {
                getLogger().warning("Failed to register Vault Economy service: " + e.getMessage());
            }
        } else {
            getLogger().info("Vault plugin not detected. Economy operating standalone.");
        }

        // PlaceholderAPI Expansion Registration
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new PAPIExpansion(this).register();
            getLogger().info("Registered PlaceholderAPI Expansion (%economy_*%)!");
        }

        // Schedule Auto-Save task every 5 minutes
        long autoSaveTicks = 20L * 60L * getConfig().getLong("economy.auto-save-interval-minutes", 5);
        Bukkit.getScheduler().runTaskTimerAsynchronously(this, () -> {
            if (profileManager != null) {
                profileManager.saveProfiles();
                getLogger().info("Auto-saved player economy profiles.");
            }
        }, autoSaveTicks, autoSaveTicks);

        // Schedule Daily Bank Interest Task
        if (getConfig().getBoolean("bank.enabled", true)) {
            long hours = getConfig().getLong("bank.interest-interval-hours", 24);
            long interestTicks = 20L * 3600L * Math.max(1, hours);
            Bukkit.getScheduler().runTaskTimer(this, () -> {
                if (profileManager != null) {
                    profileManager.applyInterestToAllProfiles();
                }
            }, interestTicks, interestTicks);
        }

        getLogger().info("Economy & Profile Management Plugin successfully enabled!");
    }

    @Override
    public void onDisable() {
        if (profileManager != null) {
            profileManager.saveProfiles();
            getLogger().info("Saved all player economy profiles on disable.");
        }
    }

    public ProfileManager getProfileManager() {
        return profileManager;
    }

    public ProfileGUI getProfileGUI() {
        return profileGUI;
    }

    public ThemeManager getThemeManager() {
        return themeManager;
    }

    public ChatInputListener getChatInputListener() {
        return chatInputListener;
    }
}
