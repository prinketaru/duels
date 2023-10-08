package dev.prinke.duels.listeners;

import dev.prinke.duels.Duels;
import dev.prinke.duels.utils.UpdateStats;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public class DeathListener implements Listener {

    Duels plugin;
    UpdateStats updateStats = new UpdateStats();

    public DeathListener(Duels plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent e) {
        if (plugin.dueling.contains(e.getEntity().getPlayer().getUniqueId())) {

            // get the kit
            String kitName = plugin.selectedKits.get(e.getEntity().getPlayer());
            // get opponent
            Player opponent = plugin.inDuel.get(e.getEntity().getPlayer());

            // update the player's loses
            updateStats.updateLoses(e.getEntity().getPlayer(), kitName, 1);
            // update the opponent's wins
            updateStats.updateWins(opponent, kitName, 1);

            // respawn the player
            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> e.getEntity().getPlayer().spigot().respawn(), 1L);

            // remove the player's grave
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                Location deathLocation = e.getEntity().getPlayer().getLastDeathLocation();
                for (int x = -1; x <= 1; x++) {
                    for (int y = -1; y <= 1; y++) {
                        for (int z = -1; z <= 1; z++) {
                            Location checkLocation = deathLocation.clone().add(x, y, z);
                            Block block = checkLocation.getBlock();
                            if (block.getType() == Material.PLAYER_HEAD || block.getType() == Material.PLAYER_WALL_HEAD) {
                                block.setType(Material.AIR);
                            }
                        }
                    }
                }
            }, 20L);

        }
    }
}
