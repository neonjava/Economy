package neonjava.in.economy.util;

import neonjava.in.economy.Economy;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ThemeManager {

    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");

    private final Economy plugin;

    private String prefix;
    private String primary;
    private String secondary;
    private String accent;
    private String success;
    private String error;
    private String neutral;

    private Material borderGlass;
    private Material fillerGlass;

    public ThemeManager(Economy plugin) {
        this.plugin = plugin;
        reloadTheme();
    }

    public void reloadTheme() {
        FileConfiguration config = plugin.getConfig();

        String preset = config.getString("theme.preset", "LIGHT").toUpperCase();

        if (preset.equals("LIGHT") || preset.equals("PASTEL")) {
            prefix = color(config.getString("theme.prefix", "&8[&d&lEconomy&8] "));
            primary = color(config.getString("theme.primary", "&f"));
            secondary = color(config.getString("theme.secondary", "&d"));
            accent = color(config.getString("theme.accent", "&b"));
            success = color(config.getString("theme.success", "&a"));
            error = color(config.getString("theme.error", "&c"));
            neutral = color(config.getString("theme.neutral", "&7"));
            borderGlass = parseMaterial(config.getString("theme.gui-border-glass", "WHITE_STAINED_GLASS_PANE"), Material.WHITE_STAINED_GLASS_PANE);
            fillerGlass = parseMaterial(config.getString("theme.gui-filler-glass", "PURPLE_STAINED_GLASS_PANE"), Material.PURPLE_STAINED_GLASS_PANE);
        } else if (preset.equals("AMETHYST")) {
            prefix = color("&8[&d&lEconomy&8] ");
            primary = color("&d");
            secondary = color("&5");
            accent = color("&f");
            success = color("&a");
            error = color("&c");
            neutral = color("&7");
            borderGlass = Material.PURPLE_STAINED_GLASS_PANE;
            fillerGlass = Material.BLACK_STAINED_GLASS_PANE;
        } else if (preset.equals("NEON_CYAN")) {
            prefix = color("&8[&b&lEconomy&8] ");
            primary = color("&b");
            secondary = color("&9");
            accent = color("&f");
            success = color("&a");
            error = color("&c");
            neutral = color("&7");
            borderGlass = Material.CYAN_STAINED_GLASS_PANE;
            fillerGlass = Material.BLACK_STAINED_GLASS_PANE;
        } else { // Custom
            prefix = color(config.getString("theme.prefix", "&8[&d&lEconomy&8] "));
            primary = color(config.getString("theme.primary", "&f"));
            secondary = color(config.getString("theme.secondary", "&d"));
            accent = color(config.getString("theme.accent", "&b"));
            success = color(config.getString("theme.success", "&a"));
            error = color(config.getString("theme.error", "&c"));
            neutral = color(config.getString("theme.neutral", "&7"));
            borderGlass = parseMaterial(config.getString("theme.gui-border-glass", "WHITE_STAINED_GLASS_PANE"), Material.WHITE_STAINED_GLASS_PANE);
            fillerGlass = parseMaterial(config.getString("theme.gui-filler-glass", "PURPLE_STAINED_GLASS_PANE"), Material.PURPLE_STAINED_GLASS_PANE);
        }
    }

    public static String color(String text) {
        if (text == null || text.isEmpty()) return "";

        Matcher matcher = HEX_PATTERN.matcher(text);
        StringBuffer buffer = new StringBuffer();
        while (matcher.find()) {
            String hex = matcher.group(1);
            StringBuilder replacement = new StringBuilder("§x");
            for (char c : hex.toCharArray()) {
                replacement.append('§').append(c);
            }
            matcher.appendReplacement(buffer, replacement.toString());
        }
        matcher.appendTail(buffer);

        return ChatColor.translateAlternateColorCodes('&', buffer.toString());
    }

    private Material parseMaterial(String name, Material fallback) {
        if (name == null) return fallback;
        try {
            Material mat = Material.matchMaterial(name);
            return mat != null ? mat : fallback;
        } catch (Exception e) {
            return fallback;
        }
    }

    public String getPrefix() {
        return prefix;
    }

    public String getPrimary() {
        return primary;
    }

    public String getSecondary() {
        return secondary;
    }

    public String getAccent() {
        return accent;
    }

    public String getSuccess() {
        return success;
    }

    public String getError() {
        return error;
    }

    public String getNeutral() {
        return neutral;
    }

    public Material getBorderGlass() {
        return borderGlass;
    }

    public Material getFillerGlass() {
        return fillerGlass;
    }

    public String formatMessage(String text) {
        return prefix + color(text);
    }

    public String formatHeader(String title) {
        return neutral + "================ " + secondary + ChatColor.BOLD + title + " " + neutral + "================";
    }
}
