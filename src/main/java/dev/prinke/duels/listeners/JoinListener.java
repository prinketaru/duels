package dev.prinke.duels.listeners;

import dev.prinke.duels.Duels;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;

public class JoinListener implements Listener {

    Duels plugin;

    public JoinListener(Duels plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        FileConfiguration config = plugin.getConfig();

        if (config.contains("duelData." + p.getUniqueId() + ".inDuel")) {
            restoreDuelData(p);
        }
    }

    public void restoreDuelData(Player p) {
        FileConfiguration config = plugin.getConfig();

        // Restore player's inventory
        ItemStack[] inventory = (ItemStack[]) plugin.getConfig().get("duels." + p.getUniqueId() + ".inventory");
        ItemStack[] armor = (ItemStack[]) plugin.getConfig().get("duels." + p.getUniqueId() + ".armor");
        ItemStack offhand = (ItemStack) plugin.getConfig().get("duels." + p.getUniqueId() + ".offhand");

        // set exp from config
        p.setExp((float) config.getDouble("duels." + p.getUniqueId() + ".exp"));

        p.getInventory().setContents(inventory);
        p.getInventory().setArmorContents(armor);
        p.getInventory().setItemInOffHand(offhand);

        // Restore player's location
        p.teleport(plugin.getConfig().getLocation("duels." + p.getUniqueId() + ".location"));

        // Remove saved duel data since it's restored now
        config.set("duelData." + p.getUniqueId(), null);
        plugin.saveConfig();
    }

}
