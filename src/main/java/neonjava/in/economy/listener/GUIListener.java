package neonjava.in.economy.listener;

import neonjava.in.economy.Economy;
import neonjava.in.economy.gui.ProfileGUI;
import neonjava.in.economy.model.Transaction;
import neonjava.in.economy.model.UserProfile;
import neonjava.in.economy.shop.ShopCategory;
import neonjava.in.economy.shop.ShopItem;
import neonjava.in.economy.util.ThemeManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GUIListener implements Listener {

    private final Economy plugin;

    public GUIListener(Economy plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();

        String title = event.getView().getTitle();
        if (title == null) return;

        ProfileGUI gui = plugin.getProfileGUI();

        if (title.equals(gui.getShopCategoriesGuiTitle())) {
            event.setCancelled(true);
            handleShopCategoriesClick(event, player);
        } else if (title.startsWith(gui.getShopCategoryPrefix())) {
            event.setCancelled(true);
            handleShopItemsClick(event, player, title.substring(gui.getShopCategoryPrefix().length()));
        } else if (title.equals(gui.getBaltopGuiTitle())) {
            event.setCancelled(true);
            handleBaltopGUIClick(event, player);
        } else if (title.startsWith(gui.getMainGuiPrefix())) {
            event.setCancelled(true);
            handleMainGUIClick(event, player, title.substring(gui.getMainGuiPrefix().length()));
        } else if (title.startsWith(gui.getBankGuiPrefix())) {
            event.setCancelled(true);
            handleBankGUIClick(event, player, title.substring(gui.getBankGuiPrefix().length()));
        } else if (title.startsWith(gui.getDepositGuiPrefix())) {
            event.setCancelled(true);
            handleQuickAmountClick(event, player, title.substring(gui.getDepositGuiPrefix().length()), true);
        } else if (title.startsWith(gui.getWithdrawGuiPrefix())) {
            event.setCancelled(true);
            handleQuickAmountClick(event, player, title.substring(gui.getWithdrawGuiPrefix().length()), false);
        } else if (title.startsWith(gui.getHistoryGuiPrefix())) {
            event.setCancelled(true);
            handleHistoryGUIClick(event, player, title);
        }
    }

    private void handleShopCategoriesClick(InventoryClickEvent event, Player player) {
        int slot = event.getRawSlot();
        if (slot < 0 || slot >= 54) return;

        if (slot == 49) {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
            player.closeInventory();
            return;
        }

        for (ShopCategory cat : plugin.getShopManager().getCategories()) {
            if (cat.getSlot() == slot) {
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
                plugin.getProfileGUI().openShopItemsGUI(player, cat);
                return;
            }
        }
    }

    private void handleShopItemsClick(InventoryClickEvent event, Player player, String categoryName) {
        int slot = event.getRawSlot();
        if (slot < 0 || slot >= 54) return;

        if (slot == 45) { // Back to categories
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
            plugin.getProfileGUI().openShopCategoriesGUI(player);
            return;
        }

        if (slot == 49) { // Close
            player.closeInventory();
            return;
        }

        ShopCategory targetCategory = null;
        for (ShopCategory cat : plugin.getShopManager().getCategories()) {
            if (ThemeManager.color(cat.getName()).equalsIgnoreCase(categoryName) ||
                    ChatColor.stripColor(ThemeManager.color(cat.getName())).equalsIgnoreCase(ChatColor.stripColor(categoryName))) {
                targetCategory = cat;
                break;
            }
        }

        if (targetCategory == null || slot >= targetCategory.getItems().size()) return;

        ShopItem item = targetCategory.getItems().get(slot);
        UserProfile profile = plugin.getProfileManager().getOrCreateProfile(player);
        ThemeManager tm = plugin.getThemeManager();

        ClickType click = event.getClick();

        if (click.isLeftClick()) { // BUY ACTION
            if (!item.isBuyable()) {
                player.sendMessage(tm.formatMessage("&cThis item cannot be purchased."));
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                return;
            }

            int count = click.isShiftClick() ? 64 : item.getDefaultAmount();
            double totalPrice = item.getBuyPrice() * ((double) count / item.getDefaultAmount());

            if (profile.getBalance() < totalPrice) {
                player.sendMessage(tm.formatMessage("&cInsufficient balance to buy " + count + "x " + item.getDisplayName() + "! Cost: &e" + plugin.getProfileManager().formatCurrency(totalPrice)));
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                return;
            }

            profile.withdraw(totalPrice, Transaction.Type.WITHDRAW, "Bought " + count + "x " + item.getDisplayName(), "Shop");
            player.getInventory().addItem(new ItemStack(item.getMaterial(), count));

            plugin.getProfileManager().saveProfiles();
            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.2f);
            player.sendMessage(tm.formatMessage("&aPurchased &e" + count + "x " + item.getDisplayName() + " &afor &e" + plugin.getProfileManager().formatCurrency(totalPrice)));

        } else if (click.isRightClick()) { // SELL ACTION
            if (!item.isSellable()) {
                player.sendMessage(tm.formatMessage("&cThis item cannot be sold."));
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                return;
            }

            int count;
            if (click.isShiftClick()) {
                count = countItemInInventory(player, item.getMaterial());
                if (count == 0) {
                    player.sendMessage(tm.formatMessage("&cYou do not have any " + item.getDisplayName() + " in your inventory!"));
                    player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                    return;
                }
            } else {
                count = item.getDefaultAmount();
                if (countItemInInventory(player, item.getMaterial()) < count) {
                    player.sendMessage(tm.formatMessage("&cYou do not have " + count + "x " + item.getDisplayName() + " to sell."));
                    player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                    return;
                }
            }

            double totalEarned = (item.getSellPrice() / item.getDefaultAmount()) * count;
            removeItemFromInventory(player, item.getMaterial(), count);
            profile.deposit(totalEarned, Transaction.Type.DEPOSIT, "Sold " + count + "x " + item.getDisplayName(), "Shop");

            plugin.getProfileManager().saveProfiles();
            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
            player.sendMessage(tm.formatMessage("&aSold &e" + count + "x " + item.getDisplayName() + " &afor &e" + plugin.getProfileManager().formatCurrency(totalEarned)));
        }
    }

    private int countItemInInventory(Player player, Material material) {
        int count = 0;
        for (ItemStack stack : player.getInventory().getContents()) {
            if (stack != null && stack.getType() == material) {
                count += stack.getAmount();
            }
        }
        return count;
    }

    private void removeItemFromInventory(Player player, Material material, int amountToRemove) {
        int remaining = amountToRemove;
        ItemStack[] contents = player.getInventory().getContents();
        for (int i = 0; i < contents.length; i++) {
            ItemStack stack = contents[i];
            if (stack != null && stack.getType() == material) {
                if (stack.getAmount() <= remaining) {
                    remaining -= stack.getAmount();
                    player.getInventory().setItem(i, null);
                } else {
                    stack.setAmount(stack.getAmount() - remaining);
                    remaining = 0;
                }
            }
            if (remaining <= 0) break;
        }
    }

    private void handleBaltopGUIClick(InventoryClickEvent event, Player player) {
        int slot = event.getRawSlot();
        if (slot < 0 || slot >= 54) return;

        if (slot == 49) {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
            player.closeInventory();
            return;
        }

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() != Material.PLAYER_HEAD) return;

        if (clicked.hasItemMeta() && clicked.getItemMeta() instanceof org.bukkit.inventory.meta.SkullMeta) {
            org.bukkit.inventory.meta.SkullMeta meta = (org.bukkit.inventory.meta.SkullMeta) clicked.getItemMeta();
            org.bukkit.OfflinePlayer owning = meta.getOwningPlayer();
            UserProfile targetProfile = null;
            if (owning != null && owning.getUniqueId() != null) {
                targetProfile = plugin.getProfileManager().getProfile(owning.getUniqueId());
            }
            if (targetProfile == null && meta.hasDisplayName()) {
                String cleanName = ChatColor.stripColor(meta.getDisplayName());
                if (cleanName.contains(": ")) {
                    cleanName = cleanName.substring(cleanName.indexOf(": ") + 2).trim();
                }
                targetProfile = plugin.getProfileManager().getProfileByName(cleanName);
            }

            if (targetProfile != null) {
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
                plugin.getProfileGUI().openProfileGUI(player, targetProfile);
            }
        }
    }

    private void handleMainGUIClick(InventoryClickEvent event, Player player, String targetName) {
        int slot = event.getRawSlot();
        if (slot < 0 || slot >= 54) return;

        ThemeManager tm = plugin.getThemeManager();
        UserProfile targetProfile = plugin.getProfileManager().getProfileByName(targetName);
        if (targetProfile == null) {
            player.sendMessage(tm.formatMessage("&cTarget profile no longer exists."));
            player.closeInventory();
            return;
        }

        boolean canManage = player.hasPermission("economy.admin") || player.hasPermission("economy.profilemanage");

        switch (slot) {
            case 19: // Deposit
                if (!canManage) {
                    player.sendMessage(tm.formatMessage("&cYou do not have permission to deposit funds directly."));
                    player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                    return;
                }
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
                plugin.getProfileGUI().openQuickAmountGUI(player, targetProfile, true);
                break;

            case 21: // Withdraw
                if (!canManage) {
                    player.sendMessage(tm.formatMessage("&cYou do not have permission to withdraw funds directly."));
                    player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                    return;
                }
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
                plugin.getProfileGUI().openQuickAmountGUI(player, targetProfile, false);
                break;

            case 23: // Bank Savings GUI
                player.playSound(player.getLocation(), Sound.BLOCK_CHEST_OPEN, 1.0f, 1.0f);
                plugin.getProfileGUI().openBankGUI(player, targetProfile);
                break;

            case 25: // Set Balance (Triggers Custom Chat Input Prompt)
                if (!canManage) {
                    player.sendMessage(tm.formatMessage("&cYou do not have permission to modify player balance."));
                    player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                    return;
                }
                player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 0.8f, 1.2f);
                plugin.getChatInputListener().requestInput(player, targetProfile, ChatInputListener.ActionType.SET_BALANCE);
                break;

            case 29: // Transaction History
                player.playSound(player.getLocation(), Sound.ITEM_BOOK_PAGE_TURN, 1.0f, 1.0f);
                plugin.getProfileGUI().openTransactionHistoryGUI(player, targetProfile, 1);
                break;

            case 31: // Lock / Unlock Account
                if (!canManage) {
                    player.sendMessage(tm.formatMessage("&cYou do not have permission to lock profiles."));
                    player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                    return;
                }
                boolean newStatus = !targetProfile.isLocked();
                targetProfile.setLocked(newStatus);
                plugin.getProfileManager().saveProfiles();

                player.playSound(player.getLocation(), newStatus ? Sound.BLOCK_CHEST_LOCKED : Sound.BLOCK_CHEST_OPEN, 1.0f, 1.0f);
                player.sendMessage(tm.formatMessage(newStatus ? "&cAccount for &d" + targetProfile.getName() + " &chas been LOCKED." : "&aAccount for &d" + targetProfile.getName() + " &ahas been UNLOCKED."));
                plugin.getProfileGUI().openProfileGUI(player, targetProfile);
                break;

            case 33: // Refresh Data
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.5f);
                plugin.getProfileGUI().openProfileGUI(player, targetProfile);
                player.sendMessage(tm.formatMessage("&aProfile data refreshed!"));
                break;

            case 49: // Close
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
                player.closeInventory();
                break;
        }
    }

    private void handleBankGUIClick(InventoryClickEvent event, Player player, String targetName) {
        int slot = event.getRawSlot();
        if (slot < 0 || slot >= 27) return;

        UserProfile targetProfile = plugin.getProfileManager().getProfileByName(targetName);
        if (targetProfile == null) {
            player.closeInventory();
            return;
        }

        if (slot == 11) { // Deposit to bank prompt
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
            plugin.getChatInputListener().requestInput(player, targetProfile, ChatInputListener.ActionType.BANK_DEPOSIT);
        } else if (slot == 15) { // Withdraw from bank prompt
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
            plugin.getChatInputListener().requestInput(player, targetProfile, ChatInputListener.ActionType.BANK_WITHDRAW);
        } else if (slot == 18) { // Back to Profile
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
            plugin.getProfileGUI().openProfileGUI(player, targetProfile);
        } else if (slot == 26) { // Close
            player.closeInventory();
        }
    }

    private void handleQuickAmountClick(InventoryClickEvent event, Player player, String targetName, boolean isDeposit) {
        int slot = event.getRawSlot();
        if (slot < 0 || slot >= 27) return;

        ThemeManager tm = plugin.getThemeManager();
        UserProfile targetProfile = plugin.getProfileManager().getProfileByName(targetName);
        if (targetProfile == null) {
            player.sendMessage(tm.formatMessage("&cProfile not found."));
            player.closeInventory();
            return;
        }

        if (slot == 16) { // Custom Amount item clicked!
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
            plugin.getChatInputListener().requestInput(player, targetProfile, isDeposit ? ChatInputListener.ActionType.DEPOSIT_CUSTOM : ChatInputListener.ActionType.WITHDRAW_CUSTOM);
            return;
        }

        if (slot == 18) { // Back button
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
            plugin.getProfileGUI().openProfileGUI(player, targetProfile);
            return;
        }

        if (slot == 26) { // Close
            player.closeInventory();
            return;
        }

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || !clicked.hasItemMeta() || !clicked.getItemMeta().hasDisplayName()) return;

        String displayName = ChatColor.stripColor(clicked.getItemMeta().getDisplayName());
        double amount = extractAmount(displayName);

        if (amount <= 0) return;

        if (isDeposit) {
            targetProfile.deposit(amount, Transaction.Type.ADMIN_ADD, "Added via Profile Management GUI", player.getName());
            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
            player.sendMessage(tm.formatMessage("&aDeposited &e" + plugin.getProfileManager().formatCurrency(amount) + " &ainto &d" + targetProfile.getName() + "'s &aaccount."));
        } else {
            if (targetProfile.getBalance() < amount) {
                player.sendMessage(tm.formatMessage("&cInsufficient funds on profile. Balance: &e" + plugin.getProfileManager().formatCurrency(targetProfile.getBalance())));
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                return;
            }
            targetProfile.withdraw(amount, Transaction.Type.ADMIN_TAKE, "Deducted via Profile Management GUI", player.getName());
            player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1.0f, 1.0f);
            player.sendMessage(tm.formatMessage("&cWithdrew &e" + plugin.getProfileManager().formatCurrency(amount) + " &cfrom &d" + targetProfile.getName() + "'s &caccount."));
        }

        plugin.getProfileManager().saveProfiles();
        plugin.getProfileGUI().openProfileGUI(player, targetProfile);
    }

    private void handleHistoryGUIClick(InventoryClickEvent event, Player player, String title) {
        int slot = event.getRawSlot();
        if (slot < 0 || slot >= 54) return;

        Pattern pattern = Pattern.compile("Tx History: (.*?) \\(p\\.(\\d+)\\)");
        Matcher matcher = pattern.matcher(ChatColor.stripColor(title));

        String targetName = "";
        int currentPage = 1;
        if (matcher.find()) {
            targetName = matcher.group(1);
            try {
                currentPage = Integer.parseInt(matcher.group(2));
            } catch (NumberFormatException ignored) {}
        }

        UserProfile targetProfile = plugin.getProfileManager().getProfileByName(targetName);
        if (targetProfile == null) {
            player.closeInventory();
            return;
        }

        if (slot == 45) { // Previous Page
            player.playSound(player.getLocation(), Sound.ITEM_BOOK_PAGE_TURN, 1.0f, 1.0f);
            plugin.getProfileGUI().openTransactionHistoryGUI(player, targetProfile, currentPage - 1);
        } else if (slot == 49) { // Back to Profile
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
            plugin.getProfileGUI().openProfileGUI(player, targetProfile);
        } else if (slot == 53) { // Next Page
            player.playSound(player.getLocation(), Sound.ITEM_BOOK_PAGE_TURN, 1.0f, 1.0f);
            plugin.getProfileGUI().openTransactionHistoryGUI(player, targetProfile, currentPage + 1);
        }
    }

    private double extractAmount(String text) {
        try {
            String cleaned = text.replaceAll("[^0-9.]", "");
            if (cleaned.isEmpty()) return 0.0;
            return Double.parseDouble(cleaned);
        } catch (Exception e) {
            return 0.0;
        }
    }
}
