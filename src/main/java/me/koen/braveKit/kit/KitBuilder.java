package me.koen.braveKit.kit;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.conversations.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import java.util.Collections;
import java.util.concurrent.CompletableFuture;

public class KitBuilder {
    private final Plugin plugin;
    private final NamespacedKey kitNameKey;
    private final Player player;
    private final String kitName;
    private final int kitId;
    private String description;
    private ItemStack[] contents;
    private ItemStack icon;


    public KitBuilder(int kitId, Plugin plugin, NamespacedKey kitNameKey, Player player, String kitName) {
        this.plugin = plugin;
        this.kitNameKey = kitNameKey;
        this.player = player;
        this.kitName = kitName;
        this.kitId = kitId;
    }

    public CompletableFuture<Kit> buildKit() {
        if (!validateInitialConditions()) {
            future.complete(null);
            return future;
        }

        createConversationFactory()
                .withTimeout(50)
                .buildConversation(player)
                .begin();

        return future;
    }

    private boolean validateInitialConditions() {
        ItemStack playerIcon = player.getInventory().getItemInMainHand();
        if (playerIcon == null || playerIcon.getType() == Material.AIR) {
            player.sendMessage(ChatColor.RED + "You must hold an item to use as the kit icon!");
            return false;
        }

        if (!validateInventory()) {
            return false;
        }

        this.icon = createKitIcon();
        this.contents = createInventoryCopy(player.getInventory().getContents());
        return true;
    }

    private ConversationFactory createConversationFactory() {
        return new ConversationFactory(plugin)
                .withFirstPrompt(createDescriptionPrompt())
                .withLocalEcho(false)
                .withTimeout(60)
                .addConversationAbandonedListener(this::handleConversationComplete);
    }

    private StringPrompt createDescriptionPrompt() {
        return new StringPrompt() {
            @Override
            public String getPromptText(ConversationContext context) {
                return ChatColor.YELLOW + "Enter a description for the kit:";
            }

            @Override
            public Prompt acceptInput(ConversationContext context, String input) {
                if (input != null) {
                    context.setSessionData("description", input);
                }
                return Prompt.END_OF_CONVERSATION;
            }
        };
    }

    private final CompletableFuture<Kit> future = new CompletableFuture<>();

    private void handleConversationComplete(ConversationAbandonedEvent event) {
        if (!event.gracefulExit()) {
            future.complete(null);
            return;
        }

        this.description = (String) event.getContext().getSessionData("description");
        if (description == null) {
            future.complete(null);
            return;
        }

        Kit newKit = new Kit(kitId, kitName, icon, Collections.singletonList(description), contents);
        player.sendMessage(ChatColor.GREEN + "Successfully created kit: " + kitName);
        future.complete(newKit);
    }

    private boolean validateInventory() {
        ItemStack[] listItems = player.getInventory().getContents();
        for (ItemStack item : listItems) {
            if (item != null && item.getType() != Material.AIR) {
                return true;
            }
        }

        player.sendMessage(ChatColor.RED + "There are no items in your inventory!");
        return false;
    }

    private ItemStack createKitIcon() {
        ItemStack iconCopy = player.getInventory().getItemInMainHand().clone();
        ItemMeta meta = iconCopy.getItemMeta();
        if (meta != null) {
            meta.setCustomModelData(kitId);
            meta.setDisplayName(ChatColor.YELLOW + kitName + " Kit");
            meta.getPersistentDataContainer().set(kitNameKey, PersistentDataType.STRING, kitName);
            iconCopy.setItemMeta(meta);
        }
        return iconCopy;
    }

    private ItemStack[] createInventoryCopy(ItemStack[] original) {
        ItemStack[] copy = new ItemStack[original.length];
        for (int i = 0; i < original.length; i++) {
            if (original[i] != null) {
                copy[i] = original[i].clone();
            }
        }
        return copy;
    }
}