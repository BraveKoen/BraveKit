package me.koen.braveKit;

import me.koen.braveKit.KitDatabase.DatabaseManager;
import me.koen.braveKit.KitInventory.KitUI;
import me.koen.braveKit.kit.Kit;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;


public class OpenKitSelector implements CommandExecutor {
    private final DatabaseManager database;
    private Map<String, Kit> kits = new HashMap<>();
    private final NamespacedKey kitNameKey;
    private final KitUI kitUI;

    public OpenKitSelector(BraveKit plugin, DatabaseManager database) {
        this.database = database;
        this.kitNameKey = new NamespacedKey(plugin, "kit-name");
        this.kitUI = new KitUI("koenKit", kitNameKey, this);
        plugin.getServer().getPluginManager().registerEvents(kitUI, plugin);
    }

    public void givePlayerKit(Player player, String kitId) {
        String cleanId = kitId.replaceAll("§[0-9a-fk-or]", "");
        Kit kit = kits.get(cleanId);
        if(kit == null) {
            player.sendMessage("ERROR: No kit found with ID: '" + kitId + "'");
            return;
        }
        kit.giveTo(player);
    }
    public void refreshKits() {
        kits.putAll(database.getAllKits());
        kitUI.updateKits(kits);
    }

    private void CreateKit(Player player, String[] args){
        ItemStack[] listItems = player.getInventory().getContents();
        // Check for empty inventory
        boolean hasItems = false;
        for (ItemStack item : listItems) {
            if (item != null && item.getType() != Material.AIR) {
                hasItems = true;
                break;
            }
        }

        if (!hasItems) {
            player.sendMessage(ChatColor.RED + "There are no items in your inventory!");
            return;
        }

        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Usage: /CreateKit <name> <desc>");
            return;
        }

        ItemStack playerIcon = player.getInventory().getItemInMainHand();
        if (playerIcon == null || playerIcon.getType() == Material.AIR) {
            player.sendMessage(ChatColor.RED + "You must hold an item to use as the kit icon!");
            return;
        }

        // Create a copy of the icon to prevent reference issues
        ItemStack iconCopy = playerIcon.clone();
        ItemMeta meta = iconCopy.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.YELLOW + args[0] + " Kit");
            PersistentDataContainer container = meta.getPersistentDataContainer();
            container.set(kitNameKey, PersistentDataType.STRING, args[0]);
            iconCopy.setItemMeta(meta);
        }

        // Create a copy of the inventory contents to prevent reference issues
        ItemStack[] itemsCopy = new ItemStack[listItems.length];
        for (int i = 0; i < listItems.length; i++) {
            if (listItems[i] != null) {
                itemsCopy[i] = listItems[i].clone();
            }
        }

        Kit newKit = new Kit(args[0], iconCopy, Collections.singletonList(args[1]), itemsCopy);
        kits.put(args[0], newKit);
        player.sendMessage(ChatColor.GREEN + "Successfully created kit: " + args[0]);

        database.SaveKit(newKit);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be used by players!");
            return true;
        }

        Player player = (Player) sender;

        switch (label.toLowerCase()) {
            case "kits":
                kitUI.openInventory(player);
                break;
            case "createkit":
                CreateKit(player, args);
                break;
            case "refreshkits":
                refreshKits();
                break;
            default:
                player.sendMessage(ChatColor.RED + "Unknown command!");
                break;
        }
        return true;
    }
}
