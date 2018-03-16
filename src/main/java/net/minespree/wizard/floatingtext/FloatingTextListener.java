package net.minespree.wizard.floatingtext;

import net.minespree.wizard.WizardPlugin;
import net.minespree.wizard.floatingtext.types.PrivateFloatingText;
import net.minespree.wizard.floatingtext.types.PublicFloatingText;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;

public class FloatingTextListener implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        PublicFloatingText.getPublicTexts().stream().filter(text -> !text.isHidden()).forEach(text -> text.show(event.getPlayer()));
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        PublicFloatingText.getPublicTexts().forEach(text -> text.hide(event.getPlayer()));
        PrivateFloatingText.getPrivateText().forEach(text -> text.hide(event.getPlayer()));
    }

    @EventHandler
    public void onTeleport(PlayerTeleportEvent event) {
        Bukkit.getScheduler().scheduleSyncDelayedTask(WizardPlugin.getPlugin(), () -> PrivateFloatingText.getPrivateText().forEach(text -> text.update(event.getPlayer(), event.getTo(), event.getFrom())), 10L);
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if(event.getFrom().distance(event.getTo()) != 0) {
            PrivateFloatingText.getPrivateText().forEach(text -> text.update(event.getPlayer(), event.getTo(), event.getFrom()));
        }
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        PrivateFloatingText.getPrivateText().forEach(text -> text.update(event.getPlayer(), event.getRespawnLocation(), event.getPlayer().getLocation()));
    }

}
