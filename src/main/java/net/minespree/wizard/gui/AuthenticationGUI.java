package net.minespree.wizard.gui;

import net.minespree.babel.*;
import net.minespree.wizard.WizardPlugin;
import net.minespree.wizard.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.function.Consumer;

public class AuthenticationGUI {

    public static final BabelMessage strAccept = Babel.translate("accept"), strDecline = Babel.translate("decline"), back = Babel.translate("go_back");
    public static final MultiBabelMessage back_lore = Babel.translateMulti("go_back_lore");

    public static void authenticate(Player player, BabelStringMessageType title, GUI prevGui, ItemBuilder item, Consumer<Player> accept, Consumer<Player> decline) {
        InventoryGUI gui = new InventoryGUI(title.toString(player), 45, WizardPlugin.getPlugin());
        for (int i = 0; i < 6; i++) {
            gui.setItem(i > 2 ? 25 + i : 19 + i, new ItemBuilder(Material.STAINED_GLASS_PANE).displayName(strAccept).durability((short) 5), accept);
            gui.setItem(i > 2 ? 29 + i : 23 + i, new ItemBuilder(Material.STAINED_GLASS_PANE).displayName(strDecline).durability((short) 14), decline);
        }
        if(prevGui != null) {
            gui.setItem(40, new ItemBuilder(Material.BOOK).displayName(back).lore(back_lore), prevGui::open);
        }
        if(item != null) {
            gui.setItem(4, item, p -> {});
        }
        gui.setSingleUse(true);
        gui.open(player);
    }

}
