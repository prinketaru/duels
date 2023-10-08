package dev.prinke.duels;

import dev.prinke.duels.commands.*;
import dev.prinke.duels.listeners.*;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public final class Duels extends JavaPlugin {

    public ArrayList<UUID> dueling = new ArrayList<>();
    public HashMap<Player, Player> inDuel = new HashMap<>();
    public HashMap<Player, Player> duelRequests = new HashMap<>();
    public HashMap<Player, String> selectedKits = new HashMap<>();
    public HashMap<Player, Player> duelInitiators = new HashMap<>();

    @Override
    public void onEnable() {
        // Plugin startup logic
        getCommand("duel").setExecutor(new CommandManager(this));
        getCommand("restoreinventory").setExecutor(new RestoreInventoryCommand(this));

        // register listeners
        getServer().getPluginManager().registerEvents(new DeathListener(this), this);
        getServer().getPluginManager().registerEvents(new ItemDropListener(this), this);
        getServer().getPluginManager().registerEvents(new JoinListener(this), this);
        getServer().getPluginManager().registerEvents(new KitMenuListener(this), this);
        getServer().getPluginManager().registerEvents(new LeaveListener(this), this);
        getServer().getPluginManager().registerEvents(new RespawnListener(this), this);
        getServer().getPluginManager().registerEvents(new TeleportListener(this), this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public YamlConfiguration getKitConfig(String kitName) {
        File kitFile = new File("plugins/Duels/kits", kitName + ".yml");
        if (kitFile.exists()) {
            return YamlConfiguration.loadConfiguration(kitFile);
        }
        return null;
    }

    public YamlConfiguration getStatsFolder() {
        File statsFolder = new File("plugins/Duels/stats");
        if (!statsFolder.exists()) {
            statsFolder.mkdirs();
        }
        return YamlConfiguration.loadConfiguration(statsFolder);
    }
}
