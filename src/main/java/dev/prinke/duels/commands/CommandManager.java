package dev.prinke.duels.commands;

import dev.prinke.duels.Duels;
import dev.prinke.duels.commands.subcommands.*;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CommandManager implements CommandExecutor, TabCompleter {

    Duels plugin;

    private final ArrayList<SubCommand> subCommands = new ArrayList<>();

    public CommandManager(Duels plugin) {
        this.plugin = plugin;
        subCommands.add(new InviteCommand(plugin));
        subCommands.add(new AddKitCommand());
        subCommands.add(new AcceptCommand(plugin));
        subCommands.add(new ForfeitCommand(plugin));
        subCommands.add(new DelKitCommand(plugin));
        subCommands.add(new StatsCommand(plugin));
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {

        if (!(commandSender instanceof Player p)) {
            commandSender.sendMessage("Only players can use this command!");
            return true;
        }

        if (strings.length > 0) {
            for (int i = 0; i < getSubCommands().size(); i++) {
                if (strings[0].equalsIgnoreCase(getSubCommands().get(i).getName())) {
                    getSubCommands().get(i).perform(p, strings);
                }
            }
        } else {
            p.sendMessage(ChatColor.AQUA + "Duels by prinke" + ChatColor.GRAY + ".dev");
            p.sendMessage(ChatColor.RESET + "");
            for (int i = 0; i < getSubCommands().size(); i++) {
                p.sendMessage(ChatColor.AQUA + getSubCommands().get(i).getSyntax() + ChatColor.WHITE + " - " + getSubCommands().get(i).getDescription());
            }
        }

        return true;
    }

    public ArrayList<SubCommand> getSubCommands() {
        return subCommands;
    }

    // create tab complete with subcommands
    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] strings) {
        ArrayList<String> completions = new ArrayList<>();

        // If the player is typing the first argument
        if (strings.length == 1) {
            for (int i = 0; i < getSubCommands().size(); i++) {
                completions.add(getSubCommands().get(i).getName());
            }
        } else if (strings.length == 2) {
            // If the player is typing the second argument after a specific subcommand
            if (strings[0].equalsIgnoreCase("invite")) {
                // Autocomplete with online player names for the invite subcommand
                for (Player player : plugin.getServer().getOnlinePlayers()) {
                    completions.add(player.getName());
                }
            }
            if (strings[0].equalsIgnoreCase("accept")) {
                // Autocomplete with the player in the requests hashmap
                for (Player player : plugin.duelRequests.keySet()) {
                    completions.add(player.getName());
                }
            }
            if (strings[0].equalsIgnoreCase("delkit")) {
                // Autocomplete with the kit names in the kits folder
                File kitsFolder = new File("plugins/Duels/kits");
                for (File kitFile : kitsFolder.listFiles()) {
                    completions.add(kitFile.getName().replace(".yml", ""));
                }
            }
            if (strings[0].equalsIgnoreCase("stats")) {
                if (strings.length == 2) {
                    // Auto-complete for player names
                    File statsFolder = new File(plugin.getDataFolder() + File.separator + "stats");
                    File[] files = statsFolder.listFiles();

                    if (files != null) {
                        for (File file : files) {
                            if (file.isFile() && file.getName().endsWith(".yml")) {
                                String uuid = file.getName().replace(".yml", "");
                                String playerName = plugin.getServer().getOfflinePlayer(UUID.fromString(uuid)).getName();
                                completions.add(playerName);
                            }
                        }
                    }
                } else if (strings.length == 3) {
                    // Auto-complete for kits
                    File kitsFolder = new File("plugins/Duels/kits");
                    for (File kitFile : kitsFolder.listFiles()) {
                        completions.add(kitFile.getName().replace(".yml", ""));
                    }
                }
            }

        }

        return completions;
    }

}
