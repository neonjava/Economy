package neonjava.in.economy.listener;

import neonjava.in.economy.Economy;
import neonjava.in.economy.model.UserProfile;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinListener implements Listener {

    private final Economy plugin;

    public PlayerJoinListener(Economy plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UserProfile profile = plugin.getProfileManager().getOrCreateProfile(player);
        profile.setName(player.getName());
        profile.updateLastSeen();
        plugin.getProfileManager().saveProfiles();
    }
}
