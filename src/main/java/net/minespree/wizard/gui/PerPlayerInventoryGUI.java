package net.minespree.wizard.gui;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import net.minespree.babel.BabelStringMessageType;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

public class PerPlayerInventoryGUI implements GUI, Listener {
    private final Map<UUID, Inventory> inventories;
    private final Function<Player, String> titleFunction;
    private final Function[] items;
    private final BiConsumer[] clickActions;
    private final Plugin plugin;
    private boolean activated = true;
    private Consumer<Player> closeConsumer;

    public PerPlayerInventoryGUI(BabelStringMessageType title, int size, Plugin plugin) {
        this(p -> title.toString(p), size, plugin);
    }

    public PerPlayerInventoryGUI(Function<Player, String> title, int size, Plugin plugin) {
        Preconditions.checkNotNull(title, "title");
        Preconditions.checkNotNull(plugin, "plugin");

        this.titleFunction = title;
        this.clickActions = new BiConsumer[size];
        this.items = new Function[size];
        this.plugin = plugin;
        this.inventories = new HashMap<>();
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public void setCloseConsumer(Consumer<Player> closeConsumer) {
        this.closeConsumer = closeConsumer;
    }

    public void setItem(int slot, Function<Player, ItemStack> builder, BiConsumer<Player, ClickType> onClick) {
        Preconditions.checkNotNull(builder, "builder");
        Preconditions.checkNotNull(onClick, "onClick");

        Preconditions.checkArgument(slot >= 0 && slot < clickActions.length, "slot " + slot + " not in range 0-" + clickActions.length);
        this.items[slot] = builder;
        clickActions[slot] = onClick;

        refresh();
    }

    public void clear(int slot) {
        Preconditions.checkArgument(slot >= 0 && slot < clickActions.length, "slot " + slot + " not in range 0-" + clickActions.length);
        items[slot] = null;
        clickActions[slot] = null;
        refresh();
    }

    public void clear() {
        Arrays.fill(items, null);
        Arrays.fill(clickActions, null);
        inventories.values().forEach(Inventory::clear);
    }

    public void refresh(Player player, int slot) {
        if (player != null && inventories.containsKey(player.getUniqueId())) {
            Function stack = items[slot];
            if (stack != null) {
                inventories.get(player.getUniqueId()).setItem(slot, (ItemStack) stack.apply(player));
            }
        }
    }

    public void refresh(Player player) {
        if (player != null && inventories.containsKey(player.getUniqueId())) {
            for (int i = 0; i < items.length; i++) {
                Function stack = items[i];
                if (stack != null) {
                    inventories.get(player.getUniqueId()).setItem(i, (ItemStack) stack.apply(player));
                }
            }
        }
    }

    public void refresh() {
        inventories.forEach((uuid, inv) -> refresh(Bukkit.getPlayer(uuid)));
    }

    public void open(Player player) {
        // generate inventory and open
        Inventory inventory = Bukkit.createInventory(null, items.length, titleFunction.apply(player));
        for (int i = 0; i < items.length; i++) {
            Function stack = items[i];
            if (stack != null) {
                inventory.setItem(i, (ItemStack) stack.apply(player));
            }
        }
        player.openInventory(inventory);
        refresh(player);
        inventories.put(player.getUniqueId(), inventory);
    }

    public void activate() {
        if (!activated) {
            Bukkit.getPluginManager().registerEvents(this, plugin);
            activated = true;
        }
    }

    public void deactivate() {
        HandlerList.unregisterAll(this);
        activated = false;
        // Need to make a copy - blame Bukkit
        ImmutableMap.copyOf(inventories).forEach((uuid, inv) -> {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null && Objects.equals(player.getOpenInventory(), inv)) {
                player.closeInventory();
            }
        });
        // forget the inventory
        inventories.clear();
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Inventory inventory = inventories.get(event.getWhoClicked().getUniqueId());
        if (inventory == null) {
            return;
        }
        if (Objects.equals(event.getClickedInventory(), inventory)) {
            event.setCancelled(true);
            if (event.getSlot() < 0 || event.getSlot() >= clickActions.length) {
                return;
            }
            BiConsumer consumer = clickActions[event.getSlot()];
            if (consumer != null) {
                consumer.accept(event.getWhoClicked(), event.getClick());
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if(event.getInventory().equals(inventories.get(event.getPlayer().getUniqueId()))) {
            if(closeConsumer != null) {
                closeConsumer.accept((Player) event.getPlayer());
            }
            inventories.remove(event.getPlayer().getUniqueId());
        }
    }
}
