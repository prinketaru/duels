package dev.prinke.duels.listeners;

import dev.prinke.duels.Duels;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public class KitMenuListener implements Listener {

    Duels plugin;

    public KitMenuListener(Duels plugin) {
        this.plugin = plugin;
    }

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
                    TextComponent message = new TextComponent(ChatColor.DARK_AQUA + player.getName() + ChatColor.AQUA + " has sent you a duel request on kit " + ChatColor.DARK_AQUA + kitName + ChatColor.AQUA + ". " + ChatColor.DARK_AQUA + "Click here " + ChatColor.AQUA + "to accept or type /acceptduel " + player.getName());
                    message.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/duel accept " + player.getName()));
                    target.spigot().sendMessage(message);
                } else {
                }

                player.closeInventory();
            }
        }
    }

}
