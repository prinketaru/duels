package dev.prinke.duels.commands.subcommands;

import dev.prinke.duels.Duels;
import dev.prinke.duels.commands.SubCommand;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.util.HashMap;
import java.util.List;

public class InviteCommand extends SubCommand {

    Duels plugin;
    HashMap<Player, Player> duelInitiators = new HashMap<>();

    public InviteCommand(Duels plugin) {
        this.plugin = plugin;
        this.duelInitiators = plugin.duelInitiators;
    }

    @Override
    public String getName() {
        return "invite";
    }

    @Override
    public String getDescription() {
        return "Invite a player to duel";
    }

    @Override
    public String getSyntax() {
        return "/duel invite <player>";
    }

    @Override
    public void perform(Player p, String[] args) {

        if (!(p.hasPermission("duels.invite") || p.isOp())) {
            p.sendMessage(ChatColor.RED + "You do not have permission to use this command!");
            return;
        }

        if (args.length <= 1) {
            p.sendMessage(ChatColor.RED + "Invalid arguments: " + ChatColor.YELLOW + "/duel invite <player>");
            return;
        }

        Player target = Bukkit.getServer().getPlayer(args[1]);

        if (target == null) {
            p.sendMessage(ChatColor.AQUA + args[1] + ChatColor.RED + " is not online.");
            return;
        }

        if (target == p) {
            if (Math.random() < 0.25) {
                // 25% chance for a funny response
                p.sendMessage(ChatColor.RED + "Do you not have any friends?");
            } else {
                p.sendMessage(ChatColor.RED + "You can't duel yourself.");
            }
            return;
        }

        if (plugin.dueling.contains(p.getUniqueId())) {
            p.sendMessage(ChatColor.RED + "You are already in a duel.");
            return;
        }

        if (plugin.dueling.contains(target.getUniqueId())) {
            p.sendMessage(ChatColor.AQUA + target.getDisplayName() + ChatColor.RED + " is already in a duel.");
            return;
        }

        if (!(plugin.dueling.isEmpty())) {
            p.sendMessage(ChatColor.RED + "Please wait for the current match to finish.");
            return;
        }

        // clear duelinitiators
        plugin.duelInitiators.clear();

        p.sendMessage(ChatColor.AQUA + "Select a kit to duel " + ChatColor.DARK_AQUA + target.getName() + ChatColor.AQUA + ".");

        // Show the GUI to the player
        showKitsGUI(p, target);

        plugin.duelRequests.put(target, p);

    }

    private void showKitsGUI(Player p, Player target) {
        int size = 9;
        Inventory kitsGUI = plugin.getServer().createInventory(null, size, "Select a Kit");

        // Load the kits from the file
        File kitDir = new File("plugins/Duels/kits");
        File[] kitFiles = kitDir.listFiles();

        for (File kitFile : kitFiles) {
            // For simplicity, we're just adding paper as an icon for each kit.
            // You might want to customize this.
            ItemStack kitItem = new ItemStack(Material.PAPER);
            ItemMeta kitMeta = kitItem.getItemMeta();
            kitMeta.setDisplayName(kitFile.getName().replace(".yml", ""));
            kitItem.setItemMeta(kitMeta);

            kitsGUI.addItem(kitItem);
        }

        plugin.duelInitiators.put(target, p);

        p.openInventory(kitsGUI);
    }
}
