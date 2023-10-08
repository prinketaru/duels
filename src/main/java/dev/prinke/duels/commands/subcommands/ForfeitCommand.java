package dev.prinke.duels.commands.subcommands;

import dev.prinke.duels.Duels;
import dev.prinke.duels.commands.SubCommand;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class ForfeitCommand extends SubCommand {

    Duels plugin;

    public ForfeitCommand(Duels plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getName() {
        return "forfeit";
    }

    @Override
    public String getDescription() {
        return "Forfeit a duel";
    }

    @Override
    public String getSyntax() {
        return "/duel forfeit";
    }

    @Override
    public void perform(Player p, String[] args) {

        if (!(p.hasPermission("duels.forfeit") || p.isOp())) {
            p.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
            return;
        }

        if (!plugin.dueling.contains(p.getUniqueId())) {
            p.sendMessage(ChatColor.RED + "You are not in a duel.");
            return;
        }

        EndDuel(p);

        return;

    }

    public void EndDuel(Player p) {
        Player target = plugin.inDuel.get(p);

        p.sendMessage(ChatColor.RED + "You have lost the duel against " + ChatColor.DARK_AQUA + target.getDisplayName() + ChatColor.RED + "!");
        target.sendMessage(ChatColor.GREEN + "You have won the duel against " + ChatColor.DARK_AQUA + p.getDisplayName() + ChatColor.GREEN + "!");
        Bukkit.broadcastMessage(ChatColor.DARK_AQUA + target.getDisplayName() + ChatColor.AQUA + " has won a duel against " + ChatColor.DARK_AQUA + p.getDisplayName() + ChatColor.AQUA + "!");

        plugin.dueling.remove(p.getUniqueId());
        plugin.dueling.remove(target.getUniqueId());

        // teleport players back to their original locations from the config
        p.teleport(plugin.getConfig().getLocation("duels." + p.getUniqueId() + ".location"));

        // remove from induel
        plugin.inDuel.remove(p);
        plugin.inDuel.remove(target);

        if (p.getOpenInventory() != null) {
            p.closeInventory();
        }

        if (target.getOpenInventory() != null) {
            target.closeInventory();
        }

        // clear the player's inventory
        p.getInventory().clear();
        p.getInventory().setArmorContents(null);
        p.getInventory().setItemInOffHand(null);

        // restore the player's inventory from the config
        ItemStack[] inventory = (ItemStack[]) plugin.getConfig().get("duels." + p.getUniqueId() + ".inventory");
        ItemStack[] armor = (ItemStack[]) plugin.getConfig().get("duels." + p.getUniqueId() + ".armor");
        ItemStack offhand = (ItemStack) plugin.getConfig().get("duels." + p.getUniqueId() + ".offhand");

        p.getInventory().setContents(inventory);
        p.getInventory().setArmorContents(armor);
        p.getInventory().setItemInOffHand(offhand);

        // teleport players back to their original locations from the config
        target.teleport(plugin.getConfig().getLocation("duels." + target.getUniqueId() + ".location"));

        // clear the player's inventory
        target.getInventory().clear();
        target.getInventory().setArmorContents(null);
        target.getInventory().setItemInOffHand(null);

        // restore the player's inventory from the config
        ItemStack[] inventory2 = (ItemStack[]) plugin.getConfig().get("duels." + target.getUniqueId() + ".inventory");
        ItemStack[] armor2 = (ItemStack[]) plugin.getConfig().get("duels." + target.getUniqueId() + ".armor");
        ItemStack offhand2 = (ItemStack) plugin.getConfig().get("duels." + target.getUniqueId() + ".offhand");

        target.getInventory().setContents(inventory2);
        target.getInventory().setArmorContents(armor2);
        target.getInventory().setItemInOffHand(offhand2);

        // clear player's effects
        p.getActivePotionEffects().clear();
        target.getActivePotionEffects().clear();

        // wait 1s
        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
            @Override
            public void run() {
                // set levels from config
                p.setLevel(plugin.getConfig().getInt("duels." + p.getUniqueId() + ".level"));
                target.setLevel(plugin.getConfig().getInt("duels." + target.getUniqueId() + ".level"));
            }
        }, 20L);

        // fill saturation, food, and health
        p.setSaturation(20);
        p.setFoodLevel(20);
        p.setHealth(20);
        target.setSaturation(20);
        target.setFoodLevel(20);
        target.setHealth(20);

        // set players to survival
        p.setGameMode(org.bukkit.GameMode.SURVIVAL);
        target.setGameMode(org.bukkit.GameMode.SURVIVAL);

    }

}
