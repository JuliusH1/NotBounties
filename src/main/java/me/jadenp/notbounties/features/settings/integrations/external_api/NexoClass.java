package me.jadenp.notbounties.features.settings.integrations.external_api;

import com.nexomc.nexo.api.NexoItems;
import com.nexomc.nexo.items.ItemBuilder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * Wrapper for the Nexo plugin API.
 * Nexo items can be used as GUI materials via "nexo:<item_id>" in the material field.
 */
public class NexoClass {

    /**
     * Attempt to build an ItemStack from a Nexo item ID.
     *
     * @param itemId The Nexo item ID (the part after "nexo:")
     * @return The built ItemStack, or null if the item doesn't exist
     */
    @Nullable
    public ItemStack getItem(String itemId) {
        try {
            ItemBuilder builder = NexoItems.itemFromId(itemId);
            if (builder == null) return null;
            return builder.build();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Check whether an ItemStack is a specific Nexo item.
     *
     * @param itemStack The item to check
     * @param itemId The Nexo item ID
     * @return True if the item matches
     */
    public boolean isNexoItem(ItemStack itemStack, String itemId) {
        try {
            String id = NexoItems.idFromItem(itemStack);
            return itemId.equals(id);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Check whether an ItemStack is any Nexo item.
     *
     * @param itemStack The item to check
     * @return True if the item is a Nexo item
     */
    public boolean isNexoItem(ItemStack itemStack) {
        try {
            return NexoItems.idFromItem(itemStack) != null;
        } catch (Exception e) {
            return false;
        }
    }
}