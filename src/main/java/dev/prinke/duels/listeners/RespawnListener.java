package dev.prinke.duels.listeners;

import dev.prinke.duels.Duels;
import dev.prinke.duels.commands.subcommands.ForfeitCommand;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;

public class RespawnListener implements Listener {

    Duels plugin;

    public RespawnListener(Duels plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent e) {
        if (plugin.dueling.contains(e.getPlayer().getUniqueId())) {
            // end the duel
            new ForfeitCommand(plugin).EndDuel(e.getPlayer());
            e.setRespawnLocation(plugin.getConfig().getLocation("duels." + e.getPlayer().getUniqueId() + ".location"));
        }
    }

}
