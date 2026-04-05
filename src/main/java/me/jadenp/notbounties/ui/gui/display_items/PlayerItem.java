package me.jadenp.notbounties.ui.gui.display_items;

import me.jadenp.notbounties.Leaderboard;
import me.jadenp.notbounties.data.Whitelist;
import me.jadenp.notbounties.features.ConfigOptions;
import me.jadenp.notbounties.features.LanguageOptions;
import me.jadenp.notbounties.features.settings.integrations.external_api.LocalTime;
import me.jadenp.notbounties.features.settings.money.NumberFormatting;
import me.jadenp.notbounties.ui.HeadFetcher;
import me.jadenp.notbounties.ui.gui.CustomItem;
import me.jadenp.notbounties.utils.DataManager;
import me.jadenp.notbounties.utils.LoggedPlayers;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Represents a player head item in a GUI.
 */
public class PlayerItem implements DisplayItem, AmountItem {
    private final UUID uuid;
    private final double amount;
    private final Leaderboard displayType;
    private final int index;
    private final long time;
    private final List<String> additionalLore;
    private final String name;

    @Override
    public ItemStack getFormattedItem(Player player, String headName, List<String> lore, int customModelData, String itemModel, String guiType) {
        ItemStack item = HeadFetcher.getUnloadedHead(uuid).clone();
        SkullMeta meta = (SkullMeta) item.getItemMeta();
        if (meta == null)
            return item;

        String parsedName = parseText(headName, player);
        meta.setDisplayName(CustomItem.toDisplayString(parsedName));

        lore = new ArrayList<>(lore); // copy so we don't mutate the original
        lore.addAll(additionalLore);
        List<String> loreStrings = lore.stream()
                .map(string -> CustomItem.toDisplayString(parseText(string, player)))
                .toList();
        meta.setLore(loreStrings);

        // do NOT apply customModelData or itemModel to player skull items
        // these override the player skin texture and break the skull display
        // player heads should display the actual player skin not a custom texture

        item.setItemMeta(meta);
        return item;
    }

    @Override
    public String parseText(String text, Player player) {
        if (text.contains("{tax}") || text.contains("{amount_tax}")) {
            double tax = amount * ConfigOptions.getMoney().getBountyTax()
                    + DataManager.getPlayerData(player.getUniqueId()).getWhitelist().getList().size() * Whitelist.getCost();
            double total = amount + tax;
            text = text.replace("{tax}", NumberFormatting.getCurrencyPrefix() + NumberFormatting.formatNumber(tax) + NumberFormatting.getCurrencySuffix())
                    .replace("{amount_tax}", NumberFormatting.getCurrencyPrefix() + NumberFormatting.formatNumber(total) + NumberFormatting.getCurrencySuffix());
        }
        if (!displayType.isMoney()) {
            text = text.replace("{amount}", NumberFormatting.formatNumber(amount));
        }
        text = text
                .replace("{name}", name)
                .replace("{rank}", (index + 1) + "")
                .replace("{leaderboard}", displayType.toString())
                .replace("{leaderboard_name}", displayType.getDisplayName())
                .replace("{items}", "")
                .replace("{viewer}", LanguageOptions.getMessage("player-prefix") + player.getName() + LanguageOptions.getMessage("player-suffix"));

        if (text.contains("{time}")) {
            String timeString = me.jadenp.notbounties.features.settings.integrations.external_api.LocalTime.formatTime(time, LocalTime.TimeFormat.PLAYER, player);
            text = text.replace("{time}", timeString);
        }
        text = text.replace("{amount}", NumberFormatting.getCurrencyPrefix() + NumberFormatting.formatNumber(amount) + NumberFormatting.getCurrencySuffix()).replace("{amount_plain}", NumberFormatting.formatNumber(amount));

        return LanguageOptions.parseRaw(text, Bukkit.getOfflinePlayer(uuid));
    }

    public PlayerItem(UUID uuid, double amount, Leaderboard displayType, int index, long time, List<String> additionalLore) {
        this.name = LoggedPlayers.getPlayerName(uuid);
        this.uuid = uuid;
        this.amount = amount;
        this.displayType = displayType;
        this.index = index;
        this.time = time;
        this.additionalLore = additionalLore;
    }

    public PlayerItem(String name, UUID uuid, double amount, Leaderboard displayType, int index, long time, List<String> additionalLore) {
        this.name = name;
        this.uuid = uuid;
        this.amount = amount;
        this.displayType = displayType;
        this.index = index;
        this.time = time;
        this.additionalLore = additionalLore;
    }

    public UUID getUuid() {
        return uuid;
    }

    @Override
    public double getAmount() {
        return amount;
    }
}
