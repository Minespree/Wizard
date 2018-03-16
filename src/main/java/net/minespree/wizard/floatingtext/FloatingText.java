package net.minespree.wizard.floatingtext;

import lombok.Getter;
import net.minespree.babel.BabelStringMessageType;
import org.bukkit.Location;
import org.bukkit.entity.Player;

@Getter
public abstract class FloatingText {

    protected BabelStringMessageType text;
    protected Object[] params;
    protected Location location;

    public FloatingText(Location location) {
        this.location = location.clone();
    }

    public void setText(BabelStringMessageType text, Object... params) {
        this.text = text;
        this.params = params;
    }

    public abstract void show(Player player);

    public abstract void hide(Player player);

    public abstract void remove();

}
