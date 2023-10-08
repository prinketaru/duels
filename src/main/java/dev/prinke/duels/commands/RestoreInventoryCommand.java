package dev.prinke.duels.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class RestoreInventoryCommand implements CommandExecutor {

    private final JavaPlugin plugin;

    public RestoreInventoryCommand(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length != 1) {
            sender.sendMessage("§cUsage: /restoreinventory <player_name>");
            return true;
        }

        Player target = plugin.getServer().getPlayer(args[0]);

        if (target == null) {
            sender.sendMessage("§cThe specified player is not online!");
            return true;
        }

        target.getInventory().clear();

        // Get stored inventory, armor and offhand from the config
        ItemStack[] storedInventory = (ItemStack[]) plugin.getConfig().getList("duels." + target.getUniqueId() + ".inventory").toArray(new ItemStack[0]);
        ItemStack[] storedArmor = (ItemStack[]) plugin.getConfig().getList("duels." + target.getUniqueId() + ".armor").toArray(new ItemStack[0]);
        ItemStack storedOffhand = plugin.getConfig().getItemStack("duels." + target.getUniqueId() + ".offhand");

        // Set the retrieved items, armor, and offhand to the player
        target.getInventory().setContents(storedInventory);
        target.getInventory().setArmorContents(storedArmor);
        target.getInventory().setItemInOffHand(storedOffhand);

        sender.sendMessage("§aSuccessfully restored " + target.getName() + "'s inventory!");

        return true;
    }
}
