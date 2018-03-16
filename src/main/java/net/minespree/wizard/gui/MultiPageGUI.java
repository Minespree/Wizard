package net.minespree.wizard.gui;

import com.google.common.base.Preconditions;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Table;
import lombok.Getter;
import net.minespree.babel.Babel;
import net.minespree.babel.BabelStringMessageType;
import net.minespree.wizard.WizardPlugin;
import net.minespree.wizard.util.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class MultiPageGUI implements GUI, Listener {

    // For consistency
    private static final ItemBuilder PREVIOUS_ARROW = new ItemBuilder(Material.ARROW).displayName(Babel.translate("previous_arrow"));
    private static final ItemBuilder NEXT_ARROW = new ItemBuilder(Material.ARROW).displayName(Babel.translate("next_arrow"));

    private Map<UUID, SortMethod> cachedSort = Maps.newHashMap();
    private Map<UUID, Integer> cachedMenu = Maps.newHashMap();
    private Table<UUID, Integer, PerPlayerInventoryGUI> cachedMenus = HashBasedTable.create();

    private Map<Function<Player, ItemStack>, BiConsumer<Player, ClickType>> consumers = Maps.newHashMap();
    private Map<Function<Player, ItemStack>, Object> items = Maps.newHashMap();

    private Table<Integer, Integer, Function<Player, ItemStack>> permanentItems = HashBasedTable.create();
    private Table<Integer, Integer, BiConsumer<Player, ClickType>> permanentConsumers = HashBasedTable.create();

    private BabelStringMessageType title;
    private String format;
    private int size, nextArrow, previousArrow;

    /**
     *
     * @param title
     * @param format
     * @param size
     */
    public MultiPageGUI(BabelStringMessageType title, String format, int size, int nextArrow, int previousArrow) {
        this.title = title;
        this.format = format == null ? PageFormat.NONE.getFormat() : format;
        this.size = size;
        this.nextArrow = nextArrow;
        this.previousArrow = previousArrow;

        Bukkit.getPluginManager().registerEvents(this, WizardPlugin.getPlugin());
    }

    /**
     *
     * @param title
     * @param format
     * @param size
     */
    public MultiPageGUI(BabelStringMessageType title, PageFormat format, int size, int nextArrow, int previousArrow) {
        this(title, format.getFormat(), size, nextArrow, previousArrow);
    }

    public void addItem(Function<Player, ItemStack> builder, Object data, BiConsumer<Player, ClickType> onClick) {
        items.put(builder, data);
        if(onClick != null) {
            consumers.put(builder, onClick);
        }
    }

    public void setItem(int slot, int menu, Function<Player, ItemStack> builder, BiConsumer<Player, ClickType> onClick) {
        permanentItems.put(slot, menu, builder);
        if(onClick != null) {
            permanentConsumers.put(slot, menu, onClick);
        }
    }

    public void setItem(int slot, int menu, ItemBuilder builder, BiConsumer<Player, ClickType> onClick) {
        setItem(slot, menu, builder::build, onClick);
    }

    public void setItem(int slot, int menu, ItemBuilder builder) {
        setItem(slot, menu, builder::build, null);
    }

    public void setItem(int slot, int menu, Function<Player, ItemStack> builder) {
        setItem(slot, menu, builder, null);
    }

    /**
     *
     * @param slot
     * @param builder
     */
    public void setItem(int slot, Function<Player, ItemStack> builder, BiConsumer<Player, ClickType> onClick) {
        setItem(slot, -1, builder, onClick);
    }

    /**
     *
     * @param slot
     * @param builder
     */
    public void setItem(int slot, Function<Player, ItemStack> builder) {
        setItem(slot, -1, builder);
    }

    /**
     *
     * @param slot
     * @param builder
     */
    public void setItem(int slot, ItemBuilder builder) {
        setItem(slot, -1, builder);
    }

    /**
     *
     * @param slot
     * @param builder
     * @param onClick
     */
    public void setItem(int slot, ItemBuilder builder, BiConsumer<Player, ClickType> onClick) {
        setItem(slot, -1, builder::build, onClick);
    }

    private void initialize(Map<Integer, PerPlayerInventoryGUI> guis, int menuNumber) {
        PerPlayerInventoryGUI gui = guis.get(menuNumber);
        if(guis.containsKey(menuNumber - 1)) {
            gui.setItem(previousArrow, PREVIOUS_ARROW::build, (p, type) -> {
                cachedMenu.put(p.getUniqueId(), menuNumber - 1);
                guis.get(menuNumber - 1).open(p);
            });
        }
        if(guis.containsKey(menuNumber + 1)) {
            gui.setItem(nextArrow, NEXT_ARROW::build, (p, type) -> {
                cachedMenu.put(p.getUniqueId(), menuNumber + 1);
                guis.get(menuNumber + 1).open(p);
            });
        }
        addTo(gui, -1);
        addTo(gui, menuNumber);
    }

    private void addTo(PerPlayerInventoryGUI gui, int menuNumber) {
        if(permanentItems.containsColumn(menuNumber)) {
            for (Integer slot : permanentItems.column(menuNumber).keySet()) {
                gui.setItem(slot, p -> permanentItems.get(slot, menuNumber).apply(p), permanentConsumers.contains(slot, menuNumber)
                        ? permanentConsumers.get(slot, menuNumber) : (p, type) -> {});
            }
        }
    }

    public void refresh(Player player, int slot) {
        if(cachedMenus.containsRow(player.getUniqueId())) {
            cachedMenus.get(player.getUniqueId(), cachedMenu.getOrDefault(player.getUniqueId(), 0)).refresh(player, slot);
        }
    }

    public void refresh(Player player) {
        if(cachedMenus.containsRow(player.getUniqueId())) {
            cachedMenus.get(player.getUniqueId(), cachedMenu.getOrDefault(player.getUniqueId(), 0)).refresh(player);
        }
    }

    public void refresh() {
        for (UUID uuid : cachedMenus.rowKeySet()) {
            cachedMenus.get(uuid, cachedMenu.getOrDefault(uuid, 0)).refresh(Bukkit.getPlayer(uuid));
        }
    }

    public void openOr(Player player, SortMethod method) {
        open(player, cachedSort.getOrDefault(player.getUniqueId(), method));
    }

    /**
     *
     * @param player
     */
    public void open(Player player) {
        open(player, cachedSort.getOrDefault(player.getUniqueId(), SortType.ALPHABETICAL.getMethod()));
    }

    /**
     *
     * @param player
     * @param method
     */
    public void open(Player player, SortMethod method) {
        open(player, method, 0);
    }

    /**
     *
     * @param player
     * @param type
     */
    public void open(Player player, SortType type) {
        open(player, type.getMethod());
    }

    /**
     *
     * @param player
     * @param method
     * @param menu
     */
    public void open(Player player, SortMethod method, int menu) {
        if(cachedMenus.contains(player.getUniqueId(), menu) && (!cachedSort.containsKey(player.getUniqueId()) || cachedSort.get(player.getUniqueId()) == method)) {
            cachedMenu.put(player.getUniqueId(), menu);
            cachedMenus.get(player.getUniqueId(), menu).open(player);
            return;
        }
        Preconditions.checkNotNull(player, "player does not exist");
        Preconditions.checkNotNull(method, "method does not exist");
        List<Integer> availableSlots = new ArrayList<>();
        char[] cArray = format.toCharArray();
        for (int i = 0; i < cArray.length; i++) {
            char c = cArray[i];
            if (c == 'X') {
                availableSlots.add(i);
            }
        }
        LinkedList<Function<Player, ItemStack>> orderedItems = method.apply(player, items);
        Map<Integer, PerPlayerInventoryGUI> guis = new HashMap<>();
        PerPlayerInventoryGUI gui = null;
        int currentMenu = -1, currentSlot = -1;
        for (int i = 0; i < orderedItems.size(); i++) {
            Function<Player, ItemStack> data = orderedItems.get(i);
            currentSlot = nextAvailableSlot(availableSlots, currentSlot);
            if(gui == null || currentSlot == -1) {
                if(gui != null)
                    currentSlot = nextAvailableSlot(availableSlots, currentSlot);
                currentMenu++;
                gui = new PerPlayerInventoryGUI(title, size, WizardPlugin.getPlugin());
                guis.put(currentMenu, gui);
            }
            if(!permanentItems.contains(currentSlot, currentMenu)) {
                gui.setItem(currentSlot, data, consumers.getOrDefault(data, (p, type) -> {}));
            } else {
                i--;
            }
        }
        for (Integer menuNumber : guis.keySet()) {
            initialize(guis, menuNumber);
        }
        if(guis.containsKey(menu)) {
            guis.get(menu).open(player);
            cachedSort.put(player.getUniqueId(), method);
            cachedMenu.put(player.getUniqueId(), menu);
            guis.forEach((m, g) -> cachedMenus.put(player.getUniqueId(), m, g));
        }
    }

    private int nextAvailableSlot(List<Integer> availableSlots, int current) {
        int next = -1;
        for (Integer slot : availableSlots) {
            if(slot > current) {
                return slot;
            } else {
                next = slot;
            }
        }
        return next <= current ? -1 : next;
    }

    /**
     *
     * @param player
     * @param type
     * @param menu
     */
    public void open(Player player, SortType type, int menu) {
        open(player, type.getMethod(), menu);
    }

    public void unregister() {
        cachedMenu.keySet().forEach(uuid -> Bukkit.getPlayer(uuid).closeInventory());
        cachedMenu.clear();
        cachedSort.clear();
        cachedMenus.values().forEach(PerPlayerInventoryGUI::deactivate);
        cachedMenus.clear();

        HandlerList.unregisterAll(this);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        cachedSort.remove(event.getPlayer().getUniqueId());
        cachedMenu.remove(event.getPlayer().getUniqueId());
        for (Integer col : Lists.newArrayList(cachedMenus.columnKeySet())) {
            cachedMenus.remove(event.getPlayer().getUniqueId(), col);
        }
    }

    public interface SortMethod {

        LinkedList<Function<Player, ItemStack>> apply(Player player, Map<Function<Player, ItemStack>, Object> items);

    }

    /**
     *
     */
    public enum SortType {

        RANDOM((player, items) -> Lists.newLinkedList(items.keySet())),
        ALPHABETICAL((player, items) -> {
            Map<String, Function<Player, ItemStack>> orderedBuilders = Maps.newTreeMap();
            items.forEach((data, o) -> {
                ItemStack item = data.apply(player);
                ItemMeta meta = item.getItemMeta();
                if(meta.hasDisplayName()) {
                    orderedBuilders.put(ChatColor.stripColor(meta.getDisplayName()), data);
                } else {
                    orderedBuilders.put(item.getType().name(), data);
                }
            });
            return Lists.newLinkedList(orderedBuilders.values());
        });

        @Getter
        private SortMethod method;

        SortType(SortMethod method) {
            this.method = method;
        }

    }

    /**
     * Page formating X represents where an item can be placed, anything else is a space
     */
    public enum PageFormat {

        NONE(""),
        ROW1("XXXXXXXXX"),
        ROW2("XXXXXXXXX" + "XXXXXXXXX"),
        ROW3("XXXXXXXXX" + "XXXXXXXXX" + "XXXXXXXXX"),
        ROW4("XXXXXXXXX" + "XXXXXXXXX" + "XXXXXXXXX" + "XXXXXXXXX"),
        ROW5("XXXXXXXXX" + "XXXXXXXXX" + "XXXXXXXXX" + "XXXXXXXXX" + "XXXXXXXXX"),
        ROW6("XXXXXXXXX" + "XXXXXXXXX" + "XXXXXXXXX" + "XXXXXXXXX" + "XXXXXXXXX" + "XXXXXXXXX"),

        RECTANGLE1("---------" + "-XXXXXXX-"),
        RECTANGLE2("---------" + "-XXXXXXX-" + "-XXXXXXX-"),
        RECTANGLE3("---------" + "-XXXXXXX-" + "-XXXXXXX-" + "-XXXXXXX-"),

        ;

        @Getter
        private String format;

        PageFormat(String format) {
            this.format = format;
        }

    }

}
