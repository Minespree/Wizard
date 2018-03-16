package net.minespree.wizard.floatingtext.types;

import lombok.Getter;
import net.minespree.babel.BabelStringMessageType;
import net.minespree.wizard.floatingtext.FloatingText;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;

public class PublicFloatingText extends FloatingText {

    @Getter
    private static Set<PublicFloatingText> publicTexts = new HashSet<>();

    private PrivateFloatingText floatingText;
    /**
     * Returns whether the text has been hidden from every player joining or most/all players online.
     */
    @Getter
    private boolean hidden;

    /**
     * Creates a public floating text that every player that joins can see.
     */
    public PublicFloatingText(Location location) {
        super(location);

        floatingText = new PrivateFloatingText(location);

        publicTexts.add(this);
    }

    /**
     * Changes the text and updates for every player.
     */
    public void setText(BabelStringMessageType text, Object... params) {
        super.setText(text);

        floatingText.setText(text, params);
    }

    /**
     * Show the public text to an individual player if it has been hidden.
     */
    public void show(Player player) {
        floatingText.show(player);
    }

    /**
     * Hide the public text for an individual player if it has been shown.
     */
    public void hide(Player player) {
        floatingText.hide(player);
    }

    /**
     * Show the public text to every player online and players joining.
     */
    public void show() {
        Bukkit.getOnlinePlayers().forEach(this::show);
        PrivateFloatingText.getPrivateText().add(floatingText);
        hidden = false;
    }

    /**
     * Hide the public text from every player online and players joining.
     */
    public void hide() {
        Bukkit.getOnlinePlayers().forEach(this::hide);
        PrivateFloatingText.getPrivateText().remove(floatingText);
        hidden = true;
    }

    /**
     * Remove the public text for every online player, or joining player.
     */
    public void remove() {
        floatingText.remove();
        publicTexts.remove(this);
    }
}
