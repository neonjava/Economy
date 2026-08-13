package neonjava.in.economy.shop;

import org.bukkit.Material;

public class ShopItem {

    private final Material material;
    private final String displayName;
    private final double buyPrice;
    private final double sellPrice;
    private final int defaultAmount;

    public ShopItem(Material material, String displayName, double buyPrice, double sellPrice, int defaultAmount) {
        this.material = material;
        this.displayName = displayName;
        this.buyPrice = buyPrice;
        this.sellPrice = sellPrice;
        this.defaultAmount = defaultAmount;
    }

    public Material getMaterial() {
        return material;
    }

    public String getDisplayName() {
        return displayName != null ? displayName : material.name().replace("_", " ");
    }

    public double getBuyPrice() {
        return buyPrice;
    }

    public double getSellPrice() {
        return sellPrice;
    }

    public int getDefaultAmount() {
        return defaultAmount;
    }

    public boolean isBuyable() {
        return buyPrice > 0;
    }

    public boolean isSellable() {
        return sellPrice > 0;
    }
}
