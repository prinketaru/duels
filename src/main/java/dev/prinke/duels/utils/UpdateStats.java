package dev.prinke.duels.utils;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;

public class UpdateStats {

    // method to update the player's wins
    public void updateWins(Player p, String kitName, int amount) {
        File statsFile = new File("plugins/Duels/stats", p.getUniqueId() + ".yml");
        if (statsFile.exists()) {
            YamlConfiguration statsConfig = YamlConfiguration.loadConfiguration(statsFile);
            statsConfig.set("wins." + kitName, statsConfig.getInt("wins." + kitName) + amount);
            try {
                statsConfig.save(statsFile);
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        } else {
            try {
                statsFile.createNewFile();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
            YamlConfiguration statsConfig = YamlConfiguration.loadConfiguration(statsFile);
            statsConfig.set("name", p.getName());
            statsConfig.set("wins", amount);
            statsConfig.set("loses", 0);
            statsConfig.set("wins." + kitName, amount);
            try {
                statsConfig.save(statsFile);
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }

    // method to update the player's loses
    public void updateLoses(Player p, String kitName, int amount) {
        File statsFile = new File("plugins/Duels/stats", p.getUniqueId() + ".yml");
        if (statsFile.exists()) {
            YamlConfiguration statsConfig = YamlConfiguration.loadConfiguration(statsFile);
            statsConfig.set("loses." + kitName, statsConfig.getInt("loses." + kitName) + amount);
            try {
                statsConfig.save(statsFile);
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        } else {
            try {
                statsFile.createNewFile();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
            YamlConfiguration statsConfig = YamlConfiguration.loadConfiguration(statsFile);
            statsConfig.set("name", p.getName());
            statsConfig.set("wins", 0);
            statsConfig.set("loses", amount);
            statsConfig.set("loses." + kitName, amount);
            try {
                statsConfig.save(statsFile);
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }

}
