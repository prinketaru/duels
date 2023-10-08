package dev.prinke.duels.commands.subcommands;

import dev.prinke.duels.commands.SubCommand;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;

public class AddKitCommand extends SubCommand {
    @Override
    public String getName() {
        return "addkit";
    }

    @Override
    public String getDescription() {
        return "Add a kit to the duel arena";
    }

    @Override
    public String getSyntax() {
        return "/duel addkit <name>";
    }

    @Override
    public void perform(Player p, String[] args) {

        // If there's no name provided for the kit, return
        if (args.length <= 1) {
            p.sendMessage(ChatColor.AQUA + "Please specify a name for the kit.");
            return;
        }

        String kitName = args[1];

        // Get the items from the player's inventory
        ItemStack[] inventory = p.getInventory().getContents();
        ItemStack[] armor = p.getInventory().getArmorContents();
        ItemStack offhand = p.getInventory().getItemInOffHand();

        saveKitToFile(kitName, inventory, armor, offhand);
        p.sendMessage("Kit saved successfully!");

        return;

    }

    private void saveKitToFile(String kitName, ItemStack[] inventory, ItemStack[] armor, ItemStack offhand) {
        try {
            File kitFile = new File("plugins/Duels/kits", kitName + ".yml");
            if (!kitFile.exists()) {
                kitFile.getParentFile().mkdirs();
                kitFile.createNewFile();
            }

            YamlConfiguration kitConfig = YamlConfiguration.loadConfiguration(kitFile);

            kitConfig.set("inventory", inventory);
            kitConfig.set("armor", armor);
            kitConfig.set("offhand", offhand);

            kitConfig.save(kitFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
