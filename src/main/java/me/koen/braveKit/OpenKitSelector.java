package me.koen.braveKit;

import me.koen.braveKit.KitDatabase.DatabaseManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;


public class OpenKitSelector implements CommandExecutor {
    private final DatabaseManager plugin;

    public OpenKitSelector(DatabaseManager plugin) {
        this.plugin = plugin;
    }

    public void KitSelector(Player player) {
        try {
            PreparedStatement statement = plugin.getConnection().prepareStatement(
                    "INSERT INTO player_data (uuid, username) VALUES (?, ?) " +
                            "ON DUPLICATE KEY UPDATE username = ?, last_login = CURRENT_TIMESTAMP"
            );

            statement.setString(1, player.getUniqueId().toString());
            statement.setString(2, player.getName());
            statement.setString(3, player.getName());

            statement.executeUpdate();
            player.sendMessage("Your data has been saved to the database!");

        } catch (SQLException e) {
            player.sendMessage("An error occurred while saving your data!");
        }


        //TODO add config for Inventory name
        Inventory inventory = Bukkit.createInventory(null, 9,"KitInventory");
        ItemStack item = new ItemStack(Material.DIAMOND_SWORD);
        ItemMeta meta = item.getItemMeta();
        List<String> lore = List.of("Diamond Sword");
        meta.setLore(lore);
        meta.setDisplayName(ChatColor.DARK_GRAY + "DiaPick");

        item.setItemMeta(meta);


        inventory.setItem(1,item);
        player.openInventory(inventory);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        KitSelector((Player) sender);
        return true;
    }
}
