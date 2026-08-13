package neonjava.in.economy.shop;

import org.bukkit.Material;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ShopCategory {

    private final String id;
    private final String name;
    private final Material iconMaterial;
    private final int slot;
    private final List<ShopItem> items;

    public ShopCategory(String id, String name, Material iconMaterial, int slot) {
        this.id = id;
        this.name = name;
        this.iconMaterial = iconMaterial;
        this.slot = slot;
        this.items = new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Material getIconMaterial() {
        return iconMaterial;
    }

    public int getSlot() {
        return slot;
    }

    public List<ShopItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    public void addItem(ShopItem item) {
        items.add(item);
    }
}
