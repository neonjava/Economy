package neonjava.in.economy.gui;

import neonjava.in.economy.Economy;
import neonjava.in.economy.model.Transaction;
import neonjava.in.economy.model.UserProfile;
import neonjava.in.economy.util.ThemeManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;

public class ProfileGUI {

    private final Economy plugin;

    public ProfileGUI(Economy plugin) {
        this.plugin = plugin;
    }

    public String getMainGuiPrefix() {
        return ThemeManager.color("&8Profile: " + plugin.getThemeManager().getSecondary());
    }

    public String getDepositGuiPrefix() {
        return ThemeManager.color("&8Deposit to: " + plugin.getThemeManager().getSuccess());
    }

    public String getWithdrawGuiPrefix() {
        return ThemeManager.color("&8Withdraw from: " + plugin.getThemeManager().getError());
    }

    public String getHistoryGuiPrefix() {
        return ThemeManager.color("&8Tx History: " + plugin.getThemeManager().getSecondary());
    }

    public String getBankGuiPrefix() {
        return ThemeManager.color("&8Bank Savings: " + plugin.getThemeManager().getSecondary());
    }

    public String getBaltopGuiTitle() {
        return ThemeManager.color("&8" + plugin.getThemeManager().getSecondary() + "&lTop Balances Leaderboard");
    }

    public void openBaltopGUI(Player viewer) {
        Inventory gui = Bukkit.createInventory(null, 54, getBaltopGuiTitle());

        ThemeManager tm = plugin.getThemeManager();
        ItemStack border = createItem(tm.getBorderGlass(), " ");
        ItemStack filler = createItem(tm.getFillerGlass(), " ");

        for (int i = 0; i < 54; i++) {
            if (i < 9 || i >= 45 || i % 9 == 0 || i % 9 == 8) {
                gui.setItem(i, border);
            } else {
                gui.setItem(i, filler);
            }
        }

        List<UserProfile> sortedProfiles = new ArrayList<>(plugin.getProfileManager().getAllProfiles());
        sortedProfiles.sort((p1, p2) -> Double.compare(p2.getBalance(), p1.getBalance()));

        int[] displaySlots = {
                13, // #1
                21, 22, 23, // #2, #3, #4
                29, 30, 31, 32, 33, // #5, #6, #7, #8, #9
                38, 39, 40, 41, 42  // #10 to #14
        };

        for (int i = 0; i < Math.min(sortedProfiles.size(), displaySlots.length); i++) {
            UserProfile p = sortedProfiles.get(i);
            int rank = i + 1;

            ItemStack head = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta meta = (SkullMeta) head.getItemMeta();
            if (meta != null) {
                meta.setOwningPlayer(Bukkit.getOfflinePlayer(p.getUuid()));
                String prefix;
                if (rank == 1) prefix = tm.getSecondary() + "§l🥇 Rank #1: " + tm.getPrimary();
                else if (rank == 2) prefix = tm.getSecondary() + "§l🥈 Rank #2: " + tm.getPrimary();
                else if (rank == 3) prefix = tm.getSecondary() + "§l🥉 Rank #3: " + tm.getPrimary();
                else prefix = tm.getSecondary() + "Rank #" + rank + ": " + tm.getPrimary();

                meta.setDisplayName(prefix + p.getName());

                List<String> lore = new ArrayList<>();
                lore.add(tm.getNeutral() + "==============================");
                lore.add(tm.getSecondary() + "Wallet Balance: " + tm.getSuccess() + plugin.getProfileManager().formatCurrency(p.getBalance()));
                lore.add(tm.getSecondary() + "Bank Balance: " + tm.getAccent() + plugin.getProfileManager().formatCurrency(p.getBankBalance()));
                lore.add(tm.getSecondary() + "Total Earned: " + tm.getPrimary() + plugin.getProfileManager().formatCurrency(p.getTotalEarned()));
                lore.add(tm.getSecondary() + "Total Spent: " + tm.getError() + plugin.getProfileManager().formatCurrency(p.getTotalSpent()));
                lore.add(tm.getSecondary() + "Account Status: " + (p.isLocked() ? tm.getError() + "Locked" : tm.getSuccess() + "Active"));
                lore.add(tm.getNeutral() + "==============================");
                lore.add(tm.getAccent() + "▶ Click to inspect profile");
                meta.setLore(lore);
                head.setItemMeta(meta);
            }
            gui.setItem(displaySlots[i], head);
        }

        // Slot 49: Close
        gui.setItem(49, createItem(Material.BARRIER, tm.getError() + "§lClose Leaderboard"));

        viewer.openInventory(gui);
    }

    public void openProfileGUI(Player viewer, UserProfile targetProfile) {
        ThemeManager tm = plugin.getThemeManager();
        String title = getMainGuiPrefix() + targetProfile.getName();
        Inventory gui = Bukkit.createInventory(null, 54, title);

        ItemStack border = createItem(tm.getBorderGlass(), " ");
        ItemStack filler = createItem(tm.getFillerGlass(), " ");

        for (int i = 0; i < 54; i++) {
            if (i < 9 || i >= 45 || i % 9 == 0 || i % 9 == 8) {
                gui.setItem(i, border);
            } else {
                gui.setItem(i, filler);
            }
        }

        // Slot 13: Player Head / Overview
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta skullMeta = (SkullMeta) head.getItemMeta();
        if (skullMeta != null) {
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(targetProfile.getUuid());
            skullMeta.setOwningPlayer(offlinePlayer);
            skullMeta.setDisplayName(tm.getSecondary() + "§l" + targetProfile.getName() + "'s Profile");

            List<String> lore = new ArrayList<>();
            lore.add(tm.getNeutral() + "==================================");
            lore.add(tm.getSecondary() + "UUID: " + tm.getNeutral() + targetProfile.getUuid());
            lore.add(tm.getSecondary() + "Wallet Balance: " + tm.getSuccess() + plugin.getProfileManager().formatCurrency(targetProfile.getBalance()));
            lore.add(tm.getSecondary() + "Bank Savings: " + tm.getAccent() + plugin.getProfileManager().formatCurrency(targetProfile.getBankBalance()));
            lore.add(tm.getSecondary() + "Lifetime Earned: " + tm.getPrimary() + plugin.getProfileManager().formatCurrency(targetProfile.getTotalEarned()));
            lore.add(tm.getSecondary() + "Lifetime Spent: " + tm.getError() + plugin.getProfileManager().formatCurrency(targetProfile.getTotalSpent()));
            lore.add(tm.getSecondary() + "Account Status: " + (targetProfile.isLocked() ? tm.getError() + "§lFROZEN / LOCKED" : tm.getSuccess() + "§lACTIVE"));
            lore.add(tm.getSecondary() + "First Joined: " + tm.getAccent() + targetProfile.getFormattedFirstJoin());
            lore.add(tm.getSecondary() + "Last Active: " + tm.getAccent() + targetProfile.getFormattedLastSeen());
            lore.add(tm.getSecondary() + "Transactions: " + tm.getPrimary() + targetProfile.getTransactions().size());
            lore.add(tm.getNeutral() + "==================================");

            skullMeta.setLore(lore);
            head.setItemMeta(skullMeta);
        }
        gui.setItem(13, head);

        boolean canManage = viewer.hasPermission("economy.admin") || viewer.hasPermission("economy.profilemanage");

        // Slot 19: Deposit Money
        List<String> depositLore = new ArrayList<>();
        depositLore.add(tm.getNeutral() + "Click to add funds to profile.");
        depositLore.add("");
        depositLore.add(canManage ? tm.getSuccess() + "▶ Click to Deposit" : tm.getError() + "Requires Admin Permission");
        gui.setItem(19, createItem(Material.EMERALD, tm.getSuccess() + "§l➕ Deposit Money", depositLore));

        // Slot 21: Withdraw Money
        List<String> withdrawLore = new ArrayList<>();
        withdrawLore.add(tm.getNeutral() + "Click to deduct funds from profile.");
        withdrawLore.add("");
        withdrawLore.add(canManage ? tm.getError() + "▶ Click to Withdraw" : tm.getError() + "Requires Admin Permission");
        gui.setItem(21, createItem(Material.REDSTONE, tm.getError() + "§l➖ Withdraw Money", withdrawLore));

        // Slot 23: Bank Savings Account
        List<String> bankLore = new ArrayList<>();
        bankLore.add(tm.getNeutral() + "View bank savings account, earn interest,");
        bankLore.add(tm.getNeutral() + "and manage bank deposits/withdrawals.");
        bankLore.add(tm.getSecondary() + "Savings Balance: " + tm.getAccent() + plugin.getProfileManager().formatCurrency(targetProfile.getBankBalance()));
        bankLore.add("");
        bankLore.add(tm.getAccent() + "▶ Click to Open Bank GUI");
        gui.setItem(23, createItem(Material.CHEST, tm.getAccent() + "§l🏦 Bank Savings Account", bankLore));

        // Slot 25: Set Balance
        List<String> setLore = new ArrayList<>();
        setLore.add(tm.getNeutral() + "Click to set balance via chat prompt.");
        setLore.add("");
        setLore.add(canManage ? tm.getSecondary() + "▶ Click to Set Custom Balance" : tm.getError() + "Requires Admin Permission");
        gui.setItem(25, createItem(Material.ANVIL, tm.getSecondary() + "§l⚙️ Set Balance", setLore));

        // Slot 29: Transaction History
        List<String> historyLore = new ArrayList<>();
        historyLore.add(tm.getNeutral() + "View financial activity logs.");
        historyLore.add("");
        historyLore.add(tm.getPrimary() + "▶ Click to View History");
        gui.setItem(29, createItem(Material.BOOK, tm.getPrimary() + "§l📜 Transaction Audit Log", historyLore));

        // Slot 31: Toggle Account Status
        List<String> statusLore = new ArrayList<>();
        statusLore.add(tm.getNeutral() + "Toggle account transacting status.");
        statusLore.add(tm.getSecondary() + "Current: " + (targetProfile.isLocked() ? tm.getError() + "Locked" : tm.getSuccess() + "Active"));
        statusLore.add("");
        statusLore.add(canManage ? tm.getSecondary() + "▶ Click to Toggle Lock" : tm.getError() + "Requires Admin Permission");
        gui.setItem(31, createItem(targetProfile.isLocked() ? Material.REDSTONE_TORCH : Material.LEVER,
                tm.getSecondary() + "§l🔒 " + (targetProfile.isLocked() ? "Unlock Account" : "Lock Account"), statusLore));

        // Slot 33: Refresh Stats
        List<String> refreshLore = new ArrayList<>();
        refreshLore.add(tm.getNeutral() + "Reload and update profile data.");
        refreshLore.add("");
        refreshLore.add(tm.getPrimary() + "▶ Click to Refresh");
        gui.setItem(33, createItem(Material.CLOCK, tm.getPrimary() + "§l🔄 Refresh Data", refreshLore));

        // Slot 49: Close GUI
        gui.setItem(49, createItem(Material.BARRIER, tm.getError() + "§lClose Menu"));

        viewer.openInventory(gui);
    }

    public void openBankGUI(Player viewer, UserProfile targetProfile) {
        ThemeManager tm = plugin.getThemeManager();
        String title = getBankGuiPrefix() + targetProfile.getName();
        Inventory gui = Bukkit.createInventory(null, 27, title);

        ItemStack border = createItem(tm.getBorderGlass(), " ");
        ItemStack filler = createItem(tm.getFillerGlass(), " ");
        for (int i = 0; i < 27; i++) {
            if (i < 9 || i >= 18 || i % 9 == 0 || i % 9 == 8) {
                gui.setItem(i, border);
            } else {
                gui.setItem(i, filler);
            }
        }

        // Slot 11: Deposit to Bank
        List<String> depLore = new ArrayList<>();
        depLore.add(tm.getNeutral() + "Transfer money from wallet to bank savings.");
        depLore.add(tm.getSecondary() + "Wallet Balance: " + tm.getSuccess() + plugin.getProfileManager().formatCurrency(targetProfile.getBalance()));
        depLore.add("");
        depLore.add(tm.getSuccess() + "▶ Click to Deposit to Bank");
        gui.setItem(11, createItem(Material.GOLD_INGOT, tm.getSuccess() + "§l📥 Deposit to Bank", depLore));

        // Slot 13: Bank Overview & Interest info
        double interestRate = plugin.getConfig().getDouble("bank.interest-rate-percent", 1.5);
        List<String> infoLore = new ArrayList<>();
        infoLore.add(tm.getNeutral() + "==================================");
        infoLore.add(tm.getSecondary() + "Savings Balance: " + tm.getAccent() + plugin.getProfileManager().formatCurrency(targetProfile.getBankBalance()));
        infoLore.add(tm.getSecondary() + "Daily Interest Rate: " + tm.getSuccess() + interestRate + "%");
        infoLore.add(tm.getSecondary() + "Est. Daily Earnings: " + tm.getSuccess() + plugin.getProfileManager().formatCurrency((targetProfile.getBankBalance() * interestRate) / 100.0));
        infoLore.add(tm.getNeutral() + "==================================");
        gui.setItem(13, createItem(Material.CHEST, tm.getSecondary() + "§l🏦 Bank Savings Overview", infoLore));

        // Slot 15: Withdraw from Bank
        List<String> wthLore = new ArrayList<>();
        wthLore.add(tm.getNeutral() + "Transfer money from bank savings back to wallet.");
        wthLore.add(tm.getSecondary() + "Bank Savings: " + tm.getAccent() + plugin.getProfileManager().formatCurrency(targetProfile.getBankBalance()));
        wthLore.add("");
        wthLore.add(tm.getError() + "▶ Click to Withdraw from Bank");
        gui.setItem(15, createItem(Material.HOPPER, tm.getError() + "§l📤 Withdraw from Bank", wthLore));

        // Slot 18: Back to Profile
        gui.setItem(18, createItem(Material.ARROW, tm.getSuccess() + "◀ Back to Profile"));
        // Slot 26: Close
        gui.setItem(26, createItem(Material.BARRIER, tm.getError() + "Close"));

        viewer.openInventory(gui);
    }

    public void openQuickAmountGUI(Player viewer, UserProfile targetProfile, boolean isDeposit) {
        ThemeManager tm = plugin.getThemeManager();
        String prefix = isDeposit ? getDepositGuiPrefix() : getWithdrawGuiPrefix();
        String title = prefix + targetProfile.getName();
        Inventory gui = Bukkit.createInventory(null, 27, title);

        ItemStack border = createItem(tm.getBorderGlass(), " ");
        ItemStack filler = createItem(tm.getFillerGlass(), " ");
        for (int i = 0; i < 27; i++) {
            if (i < 9 || i >= 18 || i % 9 == 0 || i % 9 == 8) {
                gui.setItem(i, border);
            } else {
                gui.setItem(i, filler);
            }
        }

        double[] amounts = {100, 500, 1000, 5000, 10000, 50000};
        int[] slots = {10, 11, 12, 13, 14, 15};

        Material icon = isDeposit ? Material.GOLD_INGOT : Material.REDSTONE;
        String actionName = isDeposit ? tm.getSuccess() + "+ " : tm.getError() + "- ";

        for (int i = 0; i < amounts.length; i++) {
            double amt = amounts[i];
            List<String> lore = new ArrayList<>();
            lore.add(tm.getNeutral() + "Click to " + (isDeposit ? "add " : "deduct ") + plugin.getProfileManager().formatCurrency(amt));
            lore.add(tm.getSecondary() + "Target: " + tm.getAccent() + targetProfile.getName());
            lore.add("");
            lore.add(tm.getPrimary() + "▶ Click to execute");
            gui.setItem(slots[i], createItem(icon, actionName + plugin.getProfileManager().formatCurrency(amt), lore));
        }

        // Slot 16: Custom Chat Amount Prompt
        List<String> customLore = new ArrayList<>();
        customLore.add(tm.getNeutral() + "Click to enter any custom monetary");
        customLore.add(tm.getNeutral() + "amount directly via chat prompt!");
        customLore.add("");
        customLore.add(tm.getAccent() + "▶ Click for Custom Input");
        gui.setItem(16, createItem(Material.PAPER, tm.getAccent() + "§l✍ Custom Amount", customLore));

        // Slot 18: Back to Profile
        gui.setItem(18, createItem(Material.ARROW, tm.getSuccess() + "◀ Back to Profile"));
        // Slot 26: Close
        gui.setItem(26, createItem(Material.BARRIER, tm.getError() + "Close"));

        viewer.openInventory(gui);
    }

    public void openTransactionHistoryGUI(Player viewer, UserProfile targetProfile, int page) {
        ThemeManager tm = plugin.getThemeManager();
        String title = getHistoryGuiPrefix() + targetProfile.getName() + tm.getNeutral() + " (p." + page + ")";
        Inventory gui = Bukkit.createInventory(null, 54, title);

        ItemStack border = createItem(tm.getBorderGlass(), " ");
        ItemStack filler = createItem(tm.getFillerGlass(), " ");
        for (int i = 0; i < 54; i++) {
            if (i >= 45) {
                gui.setItem(i, border);
            } else {
                gui.setItem(i, filler);
            }
        }

        List<Transaction> transactions = targetProfile.getTransactions();
        int itemsPerPage = 36;
        int maxPages = (int) Math.ceil((double) transactions.size() / itemsPerPage);
        if (maxPages == 0) maxPages = 1;
        if (page < 1) page = 1;
        if (page > maxPages) page = maxPages;

        int startIndex = (page - 1) * itemsPerPage;
        int endIndex = Math.min(startIndex + itemsPerPage, transactions.size());

        for (int i = startIndex; i < endIndex; i++) {
            Transaction t = transactions.get(i);
            int slot = i - startIndex;

            Material mat;
            switch (t.getType()) {
                case DEPOSIT:
                case ADMIN_ADD:
                case TRANSFER_RECEIVED:
                    mat = Material.PAPER;
                    break;
                case WITHDRAW:
                case ADMIN_TAKE:
                case TRANSFER_SENT:
                    mat = Material.MAP;
                    break;
                default:
                    mat = Material.WRITABLE_BOOK;
                    break;
            }

            List<String> lore = new ArrayList<>();
            lore.add(tm.getNeutral() + "==============================");
            lore.add(tm.getSecondary() + "Type: " + tm.getAccent() + t.getType().getDisplayName());
            lore.add(tm.getSecondary() + "Amount: " + tm.getSuccess() + plugin.getProfileManager().formatCurrency(t.getAmount()));
            lore.add(tm.getSecondary() + "Date: " + tm.getAccent() + t.getFormattedDate());
            lore.add(tm.getSecondary() + "Source/Target: " + tm.getNeutral() + t.getRelatedPlayer());
            lore.add(tm.getSecondary() + "Note: " + tm.getNeutral() + t.getDescription());
            lore.add(tm.getNeutral() + "==============================");

            gui.setItem(slot, createItem(mat, tm.getPrimary() + "Transaction #" + (i + 1), lore));
        }

        // Slot 45: Previous Page
        if (page > 1) {
            gui.setItem(45, createItem(Material.ARROW, tm.getSuccess() + "◀ Previous Page (" + (page - 1) + ")"));
        }

        // Slot 49: Back to Profile
        gui.setItem(49, createItem(Material.BOOK, tm.getPrimary() + "◀ Back to Profile"));

        // Slot 53: Next Page
        if (page < maxPages) {
            gui.setItem(53, createItem(Material.ARROW, tm.getSuccess() + "Next Page (" + (page + 1) + ") ▶"));
        }

        viewer.openInventory(gui);
    }

    private ItemStack createItem(Material material, String name) {
        return createItem(material, name, null);
    }

    private ItemStack createItem(Material material, String name, List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            if (lore != null) {
                meta.setLore(lore);
            }
            item.setItemMeta(meta);
        }
        return item;
    }
}
