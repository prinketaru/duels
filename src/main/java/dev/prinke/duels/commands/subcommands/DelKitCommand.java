package dev.prinke.duels.commands.subcommands;

import dev.prinke.duels.Duels;
import dev.prinke.duels.commands.SubCommand;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.io.File;

public class DelKitCommand extends SubCommand {

    Duels plugin;

    public DelKitCommand(Duels plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getName() {
        return "delkit";
    }

    @Override
    public String getDescription() {
        return "Delete a kit";
    }

    @Override
    public String getSyntax() {
        return "/duel delkit <kit>";
    }

    @Override
    public void perform(Player p, String[] args) {

        if (!(p.hasPermission("duels.delkit") || p.isOp())) {
            p.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
            return;
        }

        if (args.length < 1) {
            p.sendMessage(ChatColor.AQUA + "Please specify a kit to delete.");
            return;
        }

        String kitName = args[1];

        // delete the kit yml from the kits folder
        File kitFile = new File("plugins/Duels/kits", kitName + ".yml");
        if (kitFile.exists()) {
            kitFile.delete();
            p.sendMessage(ChatColor.AQUA + "Kit " + ChatColor.DARK_AQUA + kitName + ChatColor.AQUA + " deleted successfully!");
        } else {
            p.sendMessage(ChatColor.RED + "Kit " + ChatColor.AQUA + kitName + ChatColor.RED + " does not exist.");
        }

    }
}
