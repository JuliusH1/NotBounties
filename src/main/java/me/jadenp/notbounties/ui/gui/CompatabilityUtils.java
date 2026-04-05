package me.jadenp.notbounties.ui.gui;

import me.jadenp.notbounties.features.LanguageOptions;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class CompatabilityUtils {
    /**
     * In API versions 1.20.6 and earlier, InventoryView is a class.
     * In versions 1.21 and later, it is an interface.
     * This method uses reflection to get the top Inventory object from the
     * InventoryView associated with an InventoryEvent, to avoid runtime errors.
     * @param event The generic InventoryEvent with an InventoryView to inspect.
     * @return The top Inventory object from the event's InventoryView.
     */
    public static Inventory getTopInventory(InventoryEvent event) {
        try {
            Object view = event.getView();
            Method getTopInventory = view.getClass().getMethod("getTopInventory");
            getTopInventory.setAccessible(true);
            return (Inventory) getTopInventory.invoke(view);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static Inventory getTopInventory(Player player) {
        try {
            Object view = player.getOpenInventory();
            Method getTopInventory = view.getClass().getMethod("getTopInventory");
            getTopInventory.setAccessible(true);
            return (Inventory) getTopInventory.invoke(view);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static Inventory getBottomInventory(Player player) {
        try {
            Object view = player.getOpenInventory();
            Method getBottomInventory = view.getClass().getMethod("getBottomInventory");
            getBottomInventory.setAccessible(true);
            return (Inventory) getBottomInventory.invoke(view);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static String getTitle(InventoryEvent event) {
        try {
            Object view = event.getView();
            Method getTitle = view.getClass().getMethod("getTitle");
            getTitle.setAccessible(true);
            return (String) getTitle.invoke(view);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static String getTitle(Player player) {
        try {
            Object view = player.getOpenInventory();
            
            // Try to get title as Component first (Paper 1.13+)
            try {
                Method getTitleComponent = view.getClass().getMethod("title");
                Object titleComponent = getTitleComponent.invoke(view);
                if (titleComponent instanceof net.kyori.adventure.text.Component) {
                    return LanguageOptions.componentToLegacyString((net.kyori.adventure.text.Component) titleComponent);
                }
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ignored) {
                // Not available, fall through to String getter
            }
            
            // Fallback to String getter
            Method getTitle = view.getClass().getMethod("getTitle");
            getTitle.setAccessible(true);
            return (String) getTitle.invoke(view);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Update the title of the player's currently open inventory using Paper's Component API.
     * Paper automatically handles MiniMessage parsing and font tag rendering.
     *
     * @param player The player whose open inventory title should be updated.
     * @param rawTitle The raw title string (MiniMessage / legacy codes).
     */
    public static void setTitle(Player player, String rawTitle) {
        Component titleComponent = LanguageOptions.toComponent(rawTitle);
        
        try {
            // Use Paper's native title() method to set Component titles (Paper 1.21.1+)
            // This properly parses and renders MiniMessage including font tags
            Object view = player.getOpenInventory();
            try {
                Method setTitleMethod = view.getClass().getMethod("title", net.kyori.adventure.text.Component.class);
                setTitleMethod.setAccessible(true);
                setTitleMethod.invoke(view, titleComponent);
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException titleMethodError) {
                // Fallback to deprecated setTitle with string conversion
                String renderedTitle = LanguageOptions.componentToLegacyString(titleComponent);
                Method setTitleStringMethod = view.getClass().getMethod("setTitle", String.class);
                setTitleStringMethod.setAccessible(true);
                setTitleStringMethod.invoke(view, renderedTitle);
            }
        } catch (Exception e) {
            // Final fallback: just convert component to string and use legacy method
            try {
                String renderedTitle = LanguageOptions.componentToLegacyString(titleComponent);
                player.getOpenInventory().setTitle(renderedTitle);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    public static void setCursor(Player player, ItemStack item) {
        try {
            Object view = player.getOpenInventory();
            Method setCursor = view.getClass().getMethod("setCursor", ItemStack.class);
            setCursor.setAccessible(true);
            setCursor.invoke(view, item);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static InventoryType getType(InventoryEvent event) {
        try {
            Object view = event.getView();
            Method getType = view.getClass().getMethod("getType");
            getType.setAccessible(true);
            return (InventoryType) getType.invoke(view);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static InventoryType getType(Player player) {
        try {
            Object view = player.getOpenInventory();
            Method getType = view.getClass().getMethod("getType");
            getType.setAccessible(true);
            return (InventoryType) getType.invoke(view);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Create inventories with Component titles using Paper's native API.
     * Paper automatically handles MiniMessage parsing and font tag rendering.
     * Falls back to String titles for compatibility with older Paper versions.
     */
    public static Inventory createInventory(Player player, InventoryType inventoryType, int size, String rawTitle) {
        Component titleComponent = LanguageOptions.toComponent(rawTitle);
        boolean chest = inventoryType == InventoryType.CHEST;

        try {
            // Try Paper 1.21.3+ Component API first using reflection
            // This supports font tags and proper MiniMessage rendering
            if (chest) {
                try {
                    return org.bukkit.Bukkit.createInventory(player, size, titleComponent);
                } catch (NoSuchMethodError componentMethodNotFound) {
                    // Method not available, fall back to String
                    String renderedTitle = LanguageOptions.componentToLegacyString(titleComponent);
                    return org.bukkit.Bukkit.createInventory(player, size, renderedTitle);
                }
            } else {
                try {
                    return org.bukkit.Bukkit.createInventory(player, inventoryType, titleComponent);
                } catch (NoSuchMethodError componentMethodNotFound) {
                    // Method not available, fall back to String
                    String renderedTitle = LanguageOptions.componentToLegacyString(titleComponent);
                    return org.bukkit.Bukkit.createInventory(player, inventoryType, renderedTitle);
                }
            }
        } catch (Exception e) {
            // Final fallback - convert to string and create inventory
            String renderedTitle = LanguageOptions.componentToLegacyString(titleComponent);
            return chest
                    ? org.bukkit.Bukkit.createInventory(player, size, renderedTitle)
                    : org.bukkit.Bukkit.createInventory(player, inventoryType, renderedTitle);
        }
    }
}