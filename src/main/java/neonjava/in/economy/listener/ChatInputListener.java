package neonjava.in.economy.listener;

import neonjava.in.economy.Economy;
import neonjava.in.economy.model.Transaction;
import neonjava.in.economy.model.UserProfile;
import neonjava.in.economy.util.ThemeManager;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ChatInputListener implements Listener {

    public enum ActionType {
        SET_BALANCE,
        DEPOSIT_CUSTOM,
        WITHDRAW_CUSTOM,
        BANK_DEPOSIT,
        BANK_WITHDRAW
    }

    public static class PendingInput {
        private final UUID targetUuid;
        private final ActionType action;
        private final long timestamp;

        public PendingInput(UUID targetUuid, ActionType action) {
            this.targetUuid = targetUuid;
            this.action = action;
            this.timestamp = System.currentTimeMillis();
        }

        public UUID getTargetUuid() {
            return targetUuid;
        }

        public ActionType getAction() {
            return action;
        }

        public boolean isExpired() {
            return System.currentTimeMillis() - timestamp > 45000; // 45 sec timeout
        }
    }

    private final Economy plugin;
    private final Map<UUID, PendingInput> pendingInputs = new ConcurrentHashMap<>();

    public ChatInputListener(Economy plugin) {
        this.plugin = plugin;
    }

    public void requestInput(Player player, UserProfile targetProfile, ActionType action) {
        pendingInputs.put(player.getUniqueId(), new PendingInput(targetProfile.getUuid(), action));
        player.closeInventory();

        ThemeManager tm = plugin.getThemeManager();
        player.sendMessage(tm.getNeutral() + "=================================================");
        player.sendMessage(tm.getPrimary() + "§lCustom Amount Prompt");
        player.sendMessage(tm.getSecondary() + "Please type the dollar amount in chat for " + tm.getAccent() + targetProfile.getName() + tm.getSecondary() + ":");
        player.sendMessage(tm.getNeutral() + "(Type 'cancel' to exit prompt)");
        player.sendMessage(tm.getNeutral() + "=================================================");
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.2f);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        PendingInput pending = pendingInputs.get(player.getUniqueId());
        if (pending == null) return;

        if (pending.isExpired()) {
            pendingInputs.remove(player.getUniqueId());
            return;
        }

        event.setCancelled(true);
        pendingInputs.remove(player.getUniqueId());

        String message = event.getMessage().trim();
        ThemeManager tm = plugin.getThemeManager();

        if (message.equalsIgnoreCase("cancel")) {
            player.sendMessage(tm.formatMessage("&eInput prompt cancelled."));
            reopenGUI(player, pending.getTargetUuid());
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(message.replaceAll("[^0-9.]", ""));
            if (amount <= 0 || Double.isNaN(amount) || Double.isInfinite(amount)) {
                player.sendMessage(tm.formatMessage("&cPlease enter a valid positive number."));
                reopenGUI(player, pending.getTargetUuid());
                return;
            }
        } catch (Exception e) {
            player.sendMessage(tm.formatMessage("&cInvalid numeric amount: " + message));
            reopenGUI(player, pending.getTargetUuid());
            return;
        }

        UserProfile targetProfile = plugin.getProfileManager().getProfile(pending.getTargetUuid());
        if (targetProfile == null) {
            player.sendMessage(tm.formatMessage("&cTarget profile no longer exists."));
            return;
        }

        switch (pending.getAction()) {
            case SET_BALANCE:
                targetProfile.setBalance(amount);
                targetProfile.addTransaction(new Transaction(Transaction.Type.ADMIN_SET, amount, "Set via Chat Prompt by " + player.getName(), player.getName()));
                player.sendMessage(tm.formatMessage("&aSet balance for &d" + targetProfile.getName() + " &ato &e" + plugin.getProfileManager().formatCurrency(amount)));
                break;

            case DEPOSIT_CUSTOM:
                targetProfile.deposit(amount, Transaction.Type.ADMIN_ADD, "Added via Chat Prompt by " + player.getName(), player.getName());
                player.sendMessage(tm.formatMessage("&aDeposited &e" + plugin.getProfileManager().formatCurrency(amount) + " &ainto &d" + targetProfile.getName() + "'s &aaccount."));
                break;

            case WITHDRAW_CUSTOM:
                if (targetProfile.getBalance() < amount) {
                    player.sendMessage(tm.formatMessage("&cInsufficient balance on profile. Current: &e" + plugin.getProfileManager().formatCurrency(targetProfile.getBalance())));
                } else {
                    targetProfile.withdraw(amount, Transaction.Type.ADMIN_TAKE, "Deducted via Chat Prompt by " + player.getName(), player.getName());
                    player.sendMessage(tm.formatMessage("&cWithdrew &e" + plugin.getProfileManager().formatCurrency(amount) + " &cfrom &d" + targetProfile.getName() + "'s &caccount."));
                }
                break;

            case BANK_DEPOSIT:
                if (!targetProfile.depositToBank(amount)) {
                    player.sendMessage(tm.formatMessage("&cInsufficient wallet funds to deposit into bank."));
                } else {
                    player.sendMessage(tm.formatMessage("&aDeposited &e" + plugin.getProfileManager().formatCurrency(amount) + " &ainto bank savings!"));
                }
                break;

            case BANK_WITHDRAW:
                if (!targetProfile.withdrawFromBank(amount)) {
                    player.sendMessage(tm.formatMessage("&cInsufficient bank savings balance to withdraw."));
                } else {
                    player.sendMessage(tm.formatMessage("&aWithdrew &e" + plugin.getProfileManager().formatCurrency(amount) + " &cfrom bank savings!"));
                }
                break;
        }

        plugin.getProfileManager().saveProfiles();
        reopenGUI(player, pending.getTargetUuid());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        pendingInputs.remove(event.getPlayer().getUniqueId());
    }

    private void reopenGUI(Player player, UUID targetUuid) {
        Bukkit.getScheduler().runTask(plugin, () -> {
            UserProfile p = plugin.getProfileManager().getProfile(targetUuid);
            if (p != null && player.isOnline()) {
                plugin.getProfileGUI().openProfileGUI(player, p);
            }
        });
    }
}
