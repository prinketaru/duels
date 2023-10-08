package dev.prinke.duels.commands.subcommands;

import dev.prinke.duels.Duels;
import dev.prinke.duels.commands.SubCommand;
import org.bukkit.*;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class AcceptCommand extends SubCommand {

    private final Duels plugin;
    private final ArrayList<UUID> dueling;
    private final HashMap<Player, Player> inDuel;
    private final HashMap<Player, Player> requests;

    public AcceptCommand(Duels plugin) {
        this.plugin = plugin;
        this.requests = plugin.duelRequests;
        this.dueling = plugin.dueling;
        this.inDuel = plugin.inDuel;
    }

    @Override
    public String getName() {
        return "accept";
    }

    @Override
    public String getDescription() {
        return "Accept a duel invite from a player";
    }

    @Override
    public String getSyntax() {
        return "/duel accept <player>";
    }

    @Override
    public void perform(Player p, String[] args) {

        if (!(p.hasPermission("duels.accept") || p.isOp())) {
            p.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
            return;
        }

        if (args.length <= 1) {
            p.sendMessage(ChatColor.AQUA + "Please specify a player to accept a duel request from.");
            return;
        }

        Player target = p.getServer().getPlayer(args[1]);

        if (target == null) {
            p.sendMessage(ChatColor.AQUA + args[1] + ChatColor.RED + " is not online.");
            return;
        }

        if (target == p) {
            if (Math.random() < 0.25) {
                // 25% chance for a funny response
                p.sendMessage(ChatColor.AQUA + "Do you not have any friends?");
            } else {
                p.sendMessage(ChatColor.RED + "You can't duel yourself.");
            }
            return;
        }

        if (!plugin.duelRequests.containsKey(p) || plugin.duelRequests.get(p) != target) {
            p.sendMessage(ChatColor.AQUA + target.getDisplayName() + ChatColor.RED + " has not sent you a duel request.");
            return;
        }

        if (dueling.contains(p.getUniqueId())) {
            p.sendMessage(ChatColor.RED + "You are already in a duel.");
            return;
        }

        if (dueling.contains(target.getUniqueId())) {
            p.sendMessage(ChatColor.AQUA + target.getDisplayName() + ChatColor.RED + " is already in a duel.");
            return;
        }

        if (!(plugin.dueling.isEmpty())) {
            p.sendMessage(ChatColor.RED + "Please wait for the current match to finish!");
            return;
        }

        p.sendMessage(ChatColor.AQUA + "You have accepted the duel request from " + ChatColor.DARK_AQUA + target.getDisplayName() + ChatColor.AQUA + " on kit" + ChatColor.DARK_AQUA + plugin.selectedKits.get(target) + ChatColor.AQUA + ".");
        target.sendMessage(ChatColor.DARK_AQUA + p.getName() + ChatColor.AQUA + " has accepted your duel request!");

        requests.remove(target);

        AddPlayerToDuel(p, target);

    }

    public void AddPlayerToDuel(Player p, Player target) {
        // store both player's inventories in the config
        plugin.getConfig().set("duels." + p.getUniqueId() + ".inventory", p.getInventory().getContents());
        plugin.getConfig().set("duels." + target.getUniqueId() + ".inventory", target.getInventory().getContents());

        // store both player's armor in the config
        plugin.getConfig().set("duels." + p.getUniqueId() + ".armor", p.getInventory().getArmorContents());
        plugin.getConfig().set("duels." + target.getUniqueId() + ".armor", target.getInventory().getArmorContents());

        // store both player's offhand in the config
        plugin.getConfig().set("duels." + p.getUniqueId() + ".offhand", p.getInventory().getItemInOffHand());
        plugin.getConfig().set("duels." + target.getUniqueId() + ".offhand", target.getInventory().getItemInOffHand());

        // store both player's locations in the config
        plugin.getConfig().set("duels." + p.getUniqueId() + ".location", p.getLocation());
        plugin.getConfig().set("duels." + target.getUniqueId() + ".location", target.getLocation());

        // store player levels
        plugin.getConfig().set("duels." + p.getUniqueId() + ".level", p.getLevel());
        plugin.getConfig().set("duels." + target.getUniqueId() + ".level", target.getLevel());

        // store player exp
        plugin.getConfig().set("duels." + p.getUniqueId() + ".exp", p.getExp());
        plugin.getConfig().set("duels." + target.getUniqueId() + ".exp", target.getExp());

        // clear both player's inventories
        p.getInventory().clear();
        target.getInventory().clear();

        // clear both player's armor
        p.getInventory().setArmorContents(null);
        target.getInventory().setArmorContents(null);

        // clear both player's offhand
        p.getInventory().setItemInOffHand(null);
        target.getInventory().setItemInOffHand(null);

        // teleport both players to the arena
        // TODO: replace with location from config
        World arena = plugin.getServer().getWorld("world");
        final Location pos1 = new Location(arena, 28, 64, -571, -90, 0);
        final Location pos2 = new Location(arena, 39, 64, -571, 90, 0);

        // game start
        p.setGameMode(org.bukkit.GameMode.ADVENTURE);
        target.setGameMode(org.bukkit.GameMode.ADVENTURE);
        p.setHealth(20);
        target.setHealth(20);

        p.teleport(pos1);
        target.teleport(pos2);

        // Retrieve the selected kit for the initiator
        String selectedKit = plugin.selectedKits.getOrDefault(target, null);
        if (selectedKit != null) {
            // Apply the selected kit to both players
            applyKit(p, selectedKit);
            applyKit(target, selectedKit);

            // Optionally, remove the kit selection from memory if it's not needed
            plugin.selectedKits.remove(p);
        } else {
            // give wooden sword
            ItemStack sword = new ItemStack(Material.WOODEN_SWORD);
            p.getInventory().addItem(sword);
            target.getInventory().addItem(sword);
        }

        // add players to inDuel
        inDuel.put(p, target);
        inDuel.put(target, p);

        dueling.add(p.getUniqueId());
        dueling.add(target.getUniqueId());

    }

    private void applyKit(Player player, String kitName) {
        // Get the specific kit configuration using the method we created in the Duels class
        YamlConfiguration kitConfig = plugin.getKitConfig(kitName);

        if (kitConfig == null) {
            player.sendMessage(ChatColor.RED + "The specified kit does not exist!");
            return;
        }

        // Retrieve the items, armor, and offhand from the kit
        List<ItemStack> kitItems = (List<ItemStack>) kitConfig.getList("inventory");
        List<ItemStack> kitArmor = (List<ItemStack>) kitConfig.getList("armor");
        ItemStack kitOffhand = kitConfig.getItemStack("offhand");

        if (kitItems == null || kitArmor == null || kitOffhand == null) {
            player.sendMessage(ChatColor.RED + "There was an issue loading the kit. Make sure it's correctly configured.");
            return;
        }

        // Set the retrieved items, armor, and offhand to the player
        player.getInventory().setContents(kitItems.toArray(new ItemStack[0]));
        player.getInventory().setArmorContents(kitArmor.toArray(new ItemStack[0]));
        player.getInventory().setItemInOffHand(kitOffhand);

        player.sendMessage(ChatColor.AQUA + "Applied the " + ChatColor.DARK_AQUA + kitName + ChatColor.AQUA + " kit!");
    }

}
