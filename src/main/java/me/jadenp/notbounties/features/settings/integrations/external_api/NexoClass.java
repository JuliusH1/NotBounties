package me.jadenp.notbounties.features.settings.integrations.external_api;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * Wrapper for the Nexo plugin API.
 *
 * In gui.yml you can use "material: nexo:<item_id>" for any custom-item slot.
 */
public class NexoClass {

    /**
     * Attempt to build an ItemStack from a Nexo item ID.
     *
     * @param itemId The Nexo item ID (the part after "nexo:")
     * @return The built ItemStack, or null if the item doesn't exist or Nexo is unavailable
     */
    @Nullable
    public ItemStack getItem(String itemId) {
        try {
            // Equivalent to: NexoItems.itemFromId(itemId).build()
            Class<?> nexoItems = Class.forName("com.nexomc.nexo.api.NexoItems");
            Object builder = nexoItems.getMethod("itemFromId", String.class).invoke(null, itemId);
            if (builder == null) return null;
            return (ItemStack) builder.getClass().getMethod("build").invoke(builder);
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
            // Equivalent to: itemId.equals(NexoItems.idFromItem(itemStack))
            Class<?> nexoItems = Class.forName("com.nexomc.nexo.api.NexoItems");
            Object id = nexoItems.getMethod("idFromItem", ItemStack.class).invoke(null, itemStack);
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
            Class<?> nexoItems = Class.forName("com.nexomc.nexo.api.NexoItems");
            Object id = nexoItems.getMethod("idFromItem", ItemStack.class).invoke(null, itemStack);
            return id != null;
        } catch (Exception e) {
            return false;
        }
    }
}