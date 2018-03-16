package net.minespree.wizard.gui;

import lombok.Setter;
import net.minespree.wizard.util.ItemBuilder;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.Plugin;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.Consumer;

public class InventoryGUI implements GUI, Listener {
    private final Inventory inventory;
    private final Consumer[] clickActions;
    private final Plugin plugin;
    private boolean activated = true;
    @Setter
    private boolean singleUse;

    public InventoryGUI(String title, int size, Plugin plugin) {
        Preconditions.checkNotNull(title, "title");
        Preconditions.checkNotNull(plugin, "plugin");

        this.inventory = Bukkit.createInventory(null, size, title);
        this.clickActions = new Consumer[size];
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public void setItem(int slot, ItemBuilder builder, Consumer<Player> onClick) {
        Preconditions.checkNotNull(builder, "builder");
        Preconditions.checkNotNull(onClick, "onClick");

        Preconditions.checkArgument(slot >= 0 || slot >= clickActions.length, "slot " + slot + " not in range 0-" + clickActions.length);
        this.inventory.setItem(slot, builder.build());
        clickActions[slot] = onClick;
    }

    public void clear(int slot) {
        Preconditions.checkArgument(slot >= 0 || slot >= clickActions.length, "slot " + slot + " not in range 0-" + clickActions.length);
        this.inventory.clear(slot);
        clickActions[slot] = null;
    }

    public void clear() {
        this.inventory.clear();
        Arrays.fill(clickActions, null);
    }

    public void open(Player player) {
        player.openInventory(inventory);
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

        // viewers are mutated while closing inventories - this is an issue in Bukkit
        ImmutableList.copyOf(inventory.getViewers()).forEach(HumanEntity::closeInventory);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (Objects.equals(event.getClickedInventory(), inventory)) {
            event.setCancelled(true);
            if (event.getSlot() < 0 || event.getSlot() >= clickActions.length) {
                return;
            }

            Consumer consumer = clickActions[event.getSlot()];
            if (consumer != null) {
                consumer.accept(event.getWhoClicked());
            }

            if (singleUse) {
                Bukkit.getScheduler().runTask(plugin, this::deactivate);
            }
        }
    }
}
