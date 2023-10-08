package dev.prinke.duels.listeners;

import dev.prinke.duels.Duels;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;

public class ItemDropListener implements Listener {

    Duels plugin;

    public ItemDropListener(Duels plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onItemDrop(PlayerDropItemEvent e) {
        if (plugin.dueling.contains(e.getPlayer().getUniqueId())) {
            e.setCancelled(true);
        }
    }
}
