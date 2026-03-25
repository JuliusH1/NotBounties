package me.jadenp.notbounties.ui.gui;

import me.jadenp.notbounties.features.LanguageOptions;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
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
            Method getTitle = view.getClass().getMethod("getTitle");
            getTitle.setAccessible(true);
            return (String) getTitle.invoke(view);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Update the title of the player's currently open inventory.
     * Uses inventory view title APIs when available.
     *
     * @param player The player whose open inventory title should be updated.
     * @param rawTitle The raw title string (MiniMessage / legacy codes).
     */
    public static void setTitle(Player player, String rawTitle) {
        Component titleComponent = LanguageOptions.toComponent(rawTitle);
        Object view = player.getOpenInventory();

        Method componentSetter = findTitleSetter(view.getClass(), true);
        if (componentSetter != null) {
            try {
                Object runtimeComponent = adaptComponent(titleComponent, componentSetter.getParameterTypes()[0]);
                if (runtimeComponent != null) {
                    componentSetter.invoke(view, runtimeComponent);
                    return;
                }
            } catch (IllegalAccessException | InvocationTargetException | IllegalArgumentException ignored) {
                // Fall through to string setter.
            }
        }

        Method stringSetter = findTitleSetter(view.getClass(), false);
        if (stringSetter != null) {
            String renderedTitle = LanguageOptions.componentToLegacyString(titleComponent);
            try {
                stringSetter.invoke(view, renderedTitle);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
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
     * Create inventories with Component titles when the runtime exposes Paper-style overloads.
     * Falls back to legacy string titles on Spigot-only runtimes.
     */
    public static Inventory createInventory(Player player, InventoryType inventoryType, int size, String rawTitle) {
        Component titleComponent = LanguageOptions.toComponent(rawTitle);
        boolean chest = inventoryType == InventoryType.CHEST;

        try {
            Object server = org.bukkit.Bukkit.getServer();
            Method createInventory = findCreateInventoryMethod(server.getClass(), chest);
            if (createInventory != null) {
                Class<?> runtimeComponentType = createInventory.getParameterTypes()[2];
                Object runtimeComponent = adaptComponent(titleComponent, runtimeComponentType);
                if (runtimeComponent != null) {
                    if (chest) {
                        return (Inventory) createInventory.invoke(server, player, size, runtimeComponent);
                    }
                    return (Inventory) createInventory.invoke(server, player, inventoryType, runtimeComponent);
                }
            }
        } catch (IllegalAccessException | InvocationTargetException | IllegalArgumentException ignored) {
            // Fall back to legacy string titles below.
        }

        try {
            Method createInventory = findCreateInventoryMethod(org.bukkit.Bukkit.class, chest);
            if (createInventory != null) {
                Class<?> runtimeComponentType = createInventory.getParameterTypes()[2];
                Object runtimeComponent = adaptComponent(titleComponent, runtimeComponentType);
                if (runtimeComponent != null) {
                    if (chest) {
                        return (Inventory) createInventory.invoke(null, player, size, runtimeComponent);
                    }
                    return (Inventory) createInventory.invoke(null, player, inventoryType, runtimeComponent);
                }
            }
        } catch (IllegalAccessException | InvocationTargetException | IllegalArgumentException ignored) {
            // Fall back to legacy string titles below.
        }

        String renderedTitle = LanguageOptions.componentToLegacyString(titleComponent);
        return chest
                ? org.bukkit.Bukkit.createInventory(player, size, renderedTitle)
                : org.bukkit.Bukkit.createInventory(player, inventoryType, renderedTitle);
    }

    private static Method findCreateInventoryMethod(Class<?> serverClass, boolean chest) {
        for (Method method : serverClass.getMethods()) {
            if (!method.getName().equals("createInventory")) {
                continue;
            }
            Class<?>[] parameterTypes = method.getParameterTypes();
            if (parameterTypes.length != 3 || !InventoryHolder.class.isAssignableFrom(parameterTypes[0])) {
                continue;
            }
            if (chest && parameterTypes[1] != int.class) {
                continue;
            }
            if (!chest && parameterTypes[1] != InventoryType.class) {
                continue;
            }
            if (!"Component".equals(parameterTypes[2].getSimpleName())) {
                continue;
            }
            method.setAccessible(true);
            return method;
        }
        return null;
    }

    private static Method findTitleSetter(Class<?> viewClass, boolean component) {
        for (Method method : viewClass.getMethods()) {
            if (!method.getName().equals("setTitle")) {
                continue;
            }
            Class<?>[] parameterTypes = method.getParameterTypes();
            if (parameterTypes.length != 1) {
                continue;
            }
            if (component && "Component".equals(parameterTypes[0].getSimpleName())) {
                method.setAccessible(true);
                return method;
            }
            if (!component && parameterTypes[0] == String.class) {
                method.setAccessible(true);
                return method;
            }
        }
        return null;
    }

    private static Object adaptComponent(Component component, Class<?> runtimeComponentType) {
        if (runtimeComponentType.isInstance(component)) {
            return component;
        }
        try {
            String json = net.kyori.adventure.text.serializer.gson.GsonComponentSerializer.gson().serialize(component);
            String serializerClassName = runtimeComponentType.getPackage().getName() + ".serializer.gson.GsonComponentSerializer";
            Class<?> serializerClass = Class.forName(serializerClassName);
            Object serializer = serializerClass.getMethod("gson").invoke(null);
            return serializerClass.getMethod("deserialize", String.class).invoke(serializer, json);
        } catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException | IllegalAccessException ignored) {
            return null;
        }
    }
}
