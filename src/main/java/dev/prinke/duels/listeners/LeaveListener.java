package dev.prinke.duels.listeners;

import dev.prinke.duels.Duels;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

public class LeaveListener implements Listener {

    Duels plugin;

    public LeaveListener(Duels plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerLeave(PlayerQuitEvent e) {
        Player p = e.getPlayer();
        if (plugin.dueling.contains(p.getUniqueId())) {
            Player target = plugin.inDuel.get(p);

            if (target != null) {
                // Notify the target
                target.sendMessage(ChatColor.AQUA + "You have won the duel because " + p.getName() + " left the game!");

                // Remove both players from dueling and inDuel lists
                plugin.dueling.remove(p.getUniqueId());
                plugin.dueling.remove(target.getUniqueId());
                plugin.inDuel.remove(p);
                plugin.inDuel.remove(target);

                // Close the inventory if it's open
                if (p.getOpenInventory() != null) {
                    p.closeInventory();
                }

                if (target.getOpenInventory() != null) {
                    target.closeInventory();
                }

                // Restore target's data
                target.teleport(plugin.getConfig().getLocation("duels." + target.getUniqueId() + ".location"));
                target.getInventory().clear();
                target.getInventory().setArmorContents(null);
                target.getInventory().setItemInOffHand(null);

                ItemStack[] inventoryTarget = (ItemStack[]) plugin.getConfig().get("duels." + target.getUniqueId() + ".inventory");
                ItemStack[] armorTarget = (ItemStack[]) plugin.getConfig().get("duels." + target.getUniqueId() + ".armor");
                ItemStack offhandTarget = (ItemStack) plugin.getConfig().get("duels." + target.getUniqueId() + ".offhand");

                target.getInventory().setContents(inventoryTarget);
                target.getInventory().setArmorContents(armorTarget);
                target.getInventory().setItemInOffHand(offhandTarget);
                target.setGameMode(org.bukkit.GameMode.SURVIVAL);

                // fill saturation, health, and hunger
                target.setSaturation(20);
                target.setHealth(20);
                target.setFoodLevel(20);

                // Save the data of the player who quit for restoring later
                saveDuelData(p, target);
            }
        }
    }

    public void saveDuelData(Player p, Player target) {
        FileConfiguration config = plugin.getConfig();

        // Save the target player's UUID
        config.set("duelData." + p.getUniqueId() + ".inDuel", target.getUniqueId().toString());

        // Copy player's inventory from 'duels' to 'duelData'
        config.set("duelData." + p.getUniqueId() + ".inventory", config.getList("duels." + p.getUniqueId() + ".inventory"));
        config.set("duelData." + p.getUniqueId() + ".armor", config.getList("duels." + p.getUniqueId() + ".armor"));
        config.set("duelData." + p.getUniqueId() + ".offhand", config.getItemStack("duels." + p.getUniqueId() + ".offhand"));

        Bukkit.getLogger().warning("Saved Inventory: " + config.getList("duels." + p.getUniqueId() + ".inventory"));

        // Copy player's location from 'duels' to 'duelData'
        config.set("duelData." + p.getUniqueId() + ".location", config.getLocation("duels." + p.getUniqueId() + ".location"));

        // Save the configuration
        plugin.saveConfig();
    }

}
