package dev.prinke.duels.listeners;

import dev.prinke.duels.Duels;
import dev.prinke.duels.commands.subcommands.ForfeitCommand;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MainListener implements Listener {

    Duels plugin;
    HashMap<Player, String> selectedKits;

    public MainListener(Duels plugin) {
        this.plugin = plugin;
        this.selectedKits = plugin.selectedKits;
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

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent e) {
        if (plugin.dueling.contains(e.getEntity().getPlayer().getUniqueId())) {

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
                                // Check if the head is of the dead player (you might need more checks here)
                                block.setType(Material.AIR);
                                Bukkit.getLogger().warning("Removed grave at " + checkLocation.toString());
                            }
                            Bukkit.getLogger().warning("Checked " + checkLocation.toString());
                        }
                    }
                }
            }, 20L);  // Delay of 20 ticks (1 second). Adjust as needed.

        }
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent e) {
        if (plugin.dueling.contains(e.getPlayer().getUniqueId())) {
            // end the duel
            new ForfeitCommand(plugin).EndDuel(e.getPlayer());
            e.setRespawnLocation(plugin.getConfig().getLocation("duels." + e.getPlayer().getUniqueId() + ".location"));
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerLeave(PlayerQuitEvent e) {
        Player p = e.getPlayer();
        Bukkit.getLogger().warning("PLAYER QUIT EVENT FIRED");
        if (plugin.dueling.contains(p.getUniqueId())) {
            Bukkit.getLogger().warning("PLAYER IS IN DUEL");
            Player target = plugin.inDuel.get(p);

            if(target != null) {
                Bukkit.getLogger().warning("TARGET IS NOT NULL");
                // Notify the target
                target.sendMessage(ChatColor.GREEN + "You have won the duel because " + p.getName() + " left the game!");

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
            } else {
                Bukkit.getLogger().warning("TARGET IS NULL");
            }
        }
    }


    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        FileConfiguration config = plugin.getConfig();

        if (config.contains("duelData." + p.getUniqueId() + ".inDuel")) {
            restoreDuelData(p);
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

    public void restoreDuelData(Player p) {
        FileConfiguration config = plugin.getConfig();

        // Get target player
        UUID targetUUID = UUID.fromString(config.getString("duelData." + p.getUniqueId() + ".inDuel"));
        Player target = Bukkit.getPlayer(targetUUID);

        // If target player is still online, you can restore duel state
        if (target != null && target.isOnline()) {
            // Restore player's duel state
            plugin.inDuel.put(p, target);

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
        } else {
            // Handle scenario where target player is not online
            // This can include ending the duel, declaring a winner, etc.
            p.sendMessage(ChatColor.RED + "Your duel opponent is not online! Duel has been cancelled.");
            // You can also restore player's inventory, location etc. if needed in this scenario
        }
    }

    // kits menu listener
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getView().getTitle().equals("Select a Kit")) {
            event.setCancelled(true);

            ItemStack clickedItem = event.getCurrentItem();
            Player player = (Player) event.getWhoClicked();

            if (clickedItem != null && clickedItem.getType() != Material.AIR) {
                String kitName = clickedItem.getItemMeta().getDisplayName();

                // Store the kit selection
                plugin.selectedKits.put(player, kitName);
                player.sendMessage(ChatColor.AQUA + "You selected the " + ChatColor.DARK_AQUA + kitName + ChatColor.AQUA + " kit!");

                // Check if this player is a duel initiator and send the duel request message to the target
                Player target = null;
                for (Map.Entry<Player, Player> entry : plugin.duelInitiators.entrySet()) {
                    if (entry.getValue().equals(player)) {
                        target = entry.getKey();
                        break;
                    }
                }

                if (target == null) {
                    player.sendMessage(ChatColor.RED + "Couldn't find your duel target!");
                    return;
                }
                if (target != null) {
                    Bukkit.getLogger().warning("TARGET IS NOT NULL");
                    TextComponent message = new TextComponent(ChatColor.DARK_AQUA + player.getName() + ChatColor.AQUA + " has sent you a duel request on kit " + ChatColor.DARK_AQUA + kitName + ChatColor.AQUA + ". " + ChatColor.DARK_AQUA + "Click here " + ChatColor.AQUA + "to accept or type /acceptduel " + player.getName());
                    message.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/acceptduel " + player.getName()));
                    target.spigot().sendMessage(message);
                } else {
                    Bukkit.getLogger().warning("TARGET IS NULL");
                }

                player.closeInventory();
            }
        }
    }


    // stop players from dropping items in duel
    @EventHandler
    public void onItemDrop(PlayerDropItemEvent e) {
        if (plugin.dueling.contains(e.getPlayer().getUniqueId())) {
            e.setCancelled(true);
        }
    }

}
