package neonjava.in.economy.shop;

import neonjava.in.economy.Economy;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class ShopManager {

    private final Economy plugin;
    private final File shopFile;
    private YamlConfiguration shopConfig;
    private final Map<String, ShopCategory> categories;

    public ShopManager(Economy plugin) {
        this.plugin = plugin;
        this.shopFile = new File(plugin.getDataFolder(), "shop.yml");
        this.categories = new LinkedHashMap<>();
        loadShop();
    }

    public void loadShop() {
        categories.clear();
        if (!shopFile.exists()) {
            createDefaultShopFile();
        }

        shopConfig = YamlConfiguration.loadConfiguration(shopFile);
        ConfigurationSection catSec = shopConfig.getConfigurationSection("categories");
        if (catSec == null) return;

        for (String key : catSec.getKeys(false)) {
            ConfigurationSection c = catSec.getConfigurationSection(key);
            if (c == null) continue;

            String name = c.getString("name", key);
            Material icon = parseMaterial(c.getString("icon", "STONE"), Material.STONE);
            int slot = c.getInt("slot", 10);

            ShopCategory category = new ShopCategory(key, name, icon, slot);

            ConfigurationSection itemSec = c.getConfigurationSection("items");
            if (itemSec != null) {
                for (String itemKey : itemSec.getKeys(false)) {
                    ConfigurationSection i = itemSec.getConfigurationSection(itemKey);
                    if (i == null) continue;

                    Material mat = parseMaterial(i.getString("material", itemKey), Material.STONE);
                    String displayName = i.getString("display-name", null);
                    double buy = i.getDouble("buy-price", 0.0);
                    double sell = i.getDouble("sell-price", 0.0);
                    int amount = i.getInt("amount", 1);

                    category.addItem(new ShopItem(mat, displayName, buy, sell, amount));
                }
            }

            categories.put(key.toLowerCase(), category);
        }

        plugin.getLogger().info("Loaded " + categories.size() + " shop categories.");
    }

    public ShopCategory getCategory(String id) {
        return categories.get(id.toLowerCase());
    }

    public Collection<ShopCategory> getCategories() {
        return Collections.unmodifiableCollection(categories.values());
    }

    private Material parseMaterial(String name, Material fallback) {
        try {
            Material m = Material.matchMaterial(name);
            return m != null ? m : fallback;
        } catch (Exception e) {
            return fallback;
        }
    }

    private void createDefaultShopFile() {
        try {
            plugin.saveResource("shop.yml", false);
        } catch (Exception e) {
            YamlConfiguration defaultConfig = new YamlConfiguration();
            ConfigurationSection cats = defaultConfig.createSection("categories");

            // Building Blocks
            ConfigurationSection blocks = cats.createSection("blocks");
            blocks.set("name", "&f&lBuilding Blocks");
            blocks.set("icon", "BRICKS");
            blocks.set("slot", 11);
            ConfigurationSection blockItems = blocks.createSection("items");
            addShopItemConfig(blockItems, "STONE", "Stone", 10.0, 2.0, 64);
            addShopItemConfig(blockItems, "OAK_LOG", "Oak Log", 20.0, 5.0, 16);
            addShopItemConfig(blockItems, "GLASS", "Glass", 15.0, 3.0, 16);
            addShopItemConfig(blockItems, "SMOOTH_STONE", "Smooth Stone", 25.0, 6.0, 16);

            // Minerals & Ores
            ConfigurationSection ores = cats.createSection("minerals");
            ores.set("name", "&b&lOres & Minerals");
            ores.set("icon", "DIAMOND");
            ores.set("slot", 13);
            ConfigurationSection oreItems = ores.createSection("items");
            addShopItemConfig(oreItems, "COAL", "Coal", 15.0, 5.0, 16);
            addShopItemConfig(oreItems, "IRON_INGOT", "Iron Ingot", 50.0, 15.0, 1);
            addShopItemConfig(oreItems, "GOLD_INGOT", "Gold Ingot", 100.0, 30.0, 1);
            addShopItemConfig(oreItems, "DIAMOND", "Diamond", 500.0, 150.0, 1);
            addShopItemConfig(oreItems, "EMERALD", "Emerald", 750.0, 250.0, 1);
            addShopItemConfig(oreItems, "NETHERITE_INGOT", "Netherite Ingot", 2500.0, 800.0, 1);

            // Food & Farming
            ConfigurationSection food = cats.createSection("farming");
            food.set("name", "&a&lFood & Farming");
            food.set("icon", "GOLDEN_APPLE");
            food.set("slot", 15);
            ConfigurationSection foodItems = food.createSection("items");
            addShopItemConfig(foodItems, "COOKED_BEEF", "Steak", 20.0, 4.0, 8);
            addShopItemConfig(foodItems, "GOLDEN_APPLE", "Golden Apple", 250.0, 50.0, 1);
            addShopItemConfig(foodItems, "WHEAT", "Wheat", 10.0, 2.0, 16);
            addShopItemConfig(foodItems, "SUGAR_CANE", "Sugar Cane", 15.0, 3.0, 16);

            try {
                defaultConfig.save(shopFile);
            } catch (IOException ex) {
                plugin.getLogger().severe("Could not save default shop.yml");
            }
        }
    }

    private void addShopItemConfig(ConfigurationSection parent, String mat, String name, double buy, double sell, int amt) {
        ConfigurationSection item = parent.createSection(mat);
        item.set("material", mat);
        item.set("display-name", name);
        item.set("buy-price", buy);
        item.set("sell-price", sell);
        item.set("amount", amt);
    }
}
