package dev.prinke.duels.commands.subcommands;

import dev.prinke.duels.Duels;
import dev.prinke.duels.commands.SubCommand;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.UUID;

public class StatsCommand extends SubCommand {

    Duels plugin;

    public StatsCommand(Duels plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getName() {
        return "stats";
    }

    @Override
    public String getDescription() {
        return "View a player's duel stats";
    }

    @Override
    public String getSyntax() {
        return "/duel stats <player>";
    }

    @Override
    public void perform(Player p, String[] args) {
        if (!(p.hasPermission("duels.stats") || p.isOp())) {
            p.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
            return;
        }

        if (args.length >= 2) {
            String playerName = args[1];
            UUID playerUUID = null;

            Player player = Bukkit.getPlayer(playerName);
            if (player != null) {
                playerUUID = player.getUniqueId();
            } else {
                OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerName);
                if (offlinePlayer != null) {
                    playerUUID = offlinePlayer.getUniqueId();
                } else {
                    p.sendMessage(ChatColor.AQUA + playerName + ChatColor.RED + " does not exist.");
                    return;
                }
            }

            File statsFile = new File(plugin.getDataFolder() + File.separator + "stats" + File.separator + playerUUID.toString() + ".yml");
            if (statsFile.exists()) {
                FileConfiguration config = YamlConfiguration.loadConfiguration(statsFile);

                String name = config.getString("name");
                int totalWins = 0;
                int totalLosses = 0;

                if (config.getConfigurationSection("wins") != null) {
                    for (String kit : config.getConfigurationSection("wins").getKeys(false)) {
                        int wins = config.getInt("wins." + kit);
                        // if kit does not equal Training
                        if (!kit.equals("Training")) {
                            totalWins += wins;
                        }
                    }
                }

                if (config.getConfigurationSection("loses") != null) {
                    for (String kit : config.getConfigurationSection("loses").getKeys(false)) {
                        int losses = config.getInt("loses." + kit);
                        // if kit does not equal Training
                        if (!kit.equals("Training")) {
                            totalLosses += losses;
                        }
                    }
                }

                p.sendMessage(ChatColor.DARK_AQUA + name + ChatColor.AQUA + "'s duel stats:");
                p.sendMessage("");
                p.sendMessage(ChatColor.AQUA + "Total wins: " + ChatColor.WHITE + totalWins);
                p.sendMessage(ChatColor.AQUA + "Total losses: " + ChatColor.WHITE + totalLosses);
                p.sendMessage("");
                if (totalWins > 0 && totalLosses > 0) {
                    double winLossRatio = (double) totalWins / (double) totalLosses;
                    winLossRatio = Math.round(winLossRatio * 10.0) / 10.0;
                    p.sendMessage(ChatColor.AQUA + "Win/Loss ratio: " + ChatColor.WHITE + winLossRatio);
                }

                // if the 3rd argument is a kit, show the stats for that kit
                if (args.length >= 3) {
                    String kitName = args[2].toLowerCase();
                    // capitalize the first letter of the kit name
                    kitName = kitName.substring(0, 1).toUpperCase() + kitName.substring(1);

                    int kitWins = config.getInt("wins." + kitName, 0);
                    int kitLosses = config.getInt("loses." + kitName, 0);

                    if (kitWins == 0 && kitLosses == 0) {
                        p.sendMessage(ChatColor.AQUA + playerName + ChatColor.RED + " has not played any duels on kit " + ChatColor.AQUA + kitName + ChatColor.RED + ".");
                        return;
                    }

                    p.sendMessage("");
                    p.sendMessage(ChatColor.DARK_AQUA + name + ChatColor.AQUA + "'s stats for kit " + kitName + ":");
                    p.sendMessage("");
                    p.sendMessage(ChatColor.AQUA + "Wins: " + ChatColor.WHITE + kitWins);
                    p.sendMessage(ChatColor.AQUA + "Losses: " + ChatColor.WHITE + kitLosses);
                    p.sendMessage("");

                    if (kitWins > 0 && kitLosses > 0) {
                        double kitWinLossRatio = (double) kitWins / (double) kitLosses;
                        kitWinLossRatio = Math.round(kitWinLossRatio * 10.0) / 10.0;
                        p.sendMessage(ChatColor.AQUA + "Win/Loss ratio: " + ChatColor.WHITE + kitWinLossRatio);
                    }
                }
            } else {
                p.sendMessage(ChatColor.AQUA + playerName + ChatColor.RED + " has not played any duels.");
            }
        } else {
            p.sendMessage(ChatColor.RED + "Invalid arguments: " + ChatColor.YELLOW + "/duel stats <player>");
        }
    }
}
