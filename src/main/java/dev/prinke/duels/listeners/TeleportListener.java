package dev.prinke.duels.listeners;

import dev.prinke.duels.Duels;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;

public class TeleportListener implements Listener {

    Duels plugin;

    public TeleportListener(Duels plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent e) {
        if (plugin.dueling.contains(e.getPlayer().getUniqueId())) {
            // if the teleport was by an ender pearl
            if (!(e.getCause() == PlayerTeleportEvent.TeleportCause.ENDER_PEARL)) {
                e.setCancelled(true);
                e.getPlayer().sendMessage(ChatColor.RED + "You are not allowed to teleport while in a duel!");
            }
        }
    }
}
