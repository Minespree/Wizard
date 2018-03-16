package net.minespree.wizard.util;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.minespree.babel.BabelStringMessageType;
import net.minespree.wizard.WizardPlugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

public class ScrollableItem implements Listener {

    private ItemBuilder builder;

    private Map<UUID, Integer> selected = Maps.newHashMap();
    private Map<Integer, ItemBuilder.BabelData> selections = Maps.newHashMap();
    private Map<Integer, Consumer<Player>> selectionConsumers = Maps.newHashMap();

    public ScrollableItem(ItemBuilder builder) {
        this.builder = builder;

        Bukkit.getPluginManager().registerEvents(this, WizardPlugin.getPlugin());
    }

    public ScrollableItem setSelection(int position, BabelStringMessageType message, Consumer<Player> onSelect, Object... params) {
        setSelection(position, message, params);
        selectionConsumers.put(position, onSelect);
        return this;
    }

    public ScrollableItem setSelection(int position, BabelStringMessageType message, Object... params) {
        selections.put(position, new ItemBuilder.BabelData(message, params));
        return this;
    }

    public ScrollableItem removeSelection(int position) {
        selections.remove(position);
        return this;
    }

    public Integer select(Player player, ClickType type) {
        int current = selected.getOrDefault(player.getUniqueId(), 0);
        if(type.isLeftClick()) {
            current++;
            if(current > selections.size() - 1) {
                current = 0;
            }
        } else {
            current--;
            if(current < 0) {
                current = selections.size() - 1;
            }
        }
        selected.put(player.getUniqueId(), current);
        if(selectionConsumers.containsKey(current)) {
            selectionConsumers.get(current).accept(player);
        }
        return current;
    }

    public ItemStack build(Player player) {
        ItemStack item = builder.build(player);
        ItemMeta meta = item.getItemMeta();
        List<String> lore = meta.hasLore() ? meta.getLore() : Lists.newArrayList();
        final int selected = player == null ? 0 : this.selected.getOrDefault(player.getUniqueId(), 0);
        selections.forEach((i, data) -> lore.add((selected == i ? Chat.DARK_GRAY : Chat.GRAY) + data.getType().toString(player, data.getParams())));
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        selected.remove(event.getPlayer().getUniqueId());
    }
    
}
