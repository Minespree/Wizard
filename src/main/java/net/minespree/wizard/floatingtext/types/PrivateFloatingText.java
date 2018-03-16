package net.minespree.wizard.floatingtext.types;

import com.comphenix.packetwrapper.WrapperPlayServerEntityDestroy;
import com.comphenix.packetwrapper.WrapperPlayServerEntityMetadata;
import com.comphenix.packetwrapper.WrapperPlayServerSpawnEntityLiving;
import com.comphenix.protocol.wrappers.WrappedWatchableObject;
import com.google.common.collect.Lists;
import lombok.Getter;
import net.minecraft.server.v1_8_R3.AxisAlignedBB;
import net.minecraft.server.v1_8_R3.EntityArmorStand;
import net.minespree.babel.BabelStringMessageType;
import net.minespree.wizard.WizardPlugin;
import net.minespree.wizard.floatingtext.FloatingText;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

public class PrivateFloatingText extends FloatingText {

    private static Set<Integer> usedIds = new HashSet<>();
    @Getter
    private static Set<PrivateFloatingText> privateText = new HashSet<>();

    private Map<UUID, Integer> shown = new ConcurrentHashMap<>();

    /**
     * Creates a private floating text which can only be seen by individual players.
     */
    public PrivateFloatingText(Location location) {
        super(location);

        privateText.add(this);
    }

    /**
     * Shows the private text to an individual player.
     */
    public void show(Player player) {
        if(!shown.containsKey(player.getUniqueId())) {
            EntityArmorStand stand = new EntityArmorStand(((CraftWorld) location.getWorld()).getHandle());
            stand.setPositionRotation(location.getX(), location.getY() + 2, location.getZ(), location.getYaw(), location.getPitch()); // Set y 2 blocks higher because setMarker removes bounding box
            stand.setCustomName(text == null ? "" : text.toString(player, params));
            stand.setCustomNameVisible(true);
            stand.setInvisible(true);
            stand.setGravity(false);
            stand.setBasePlate(false);
            stand.n(true); // Set as marker.
            stand.a(new AxisAlignedBB(0.0, 0.0, 0.0, 0.0, 0.0, 0.0));

            int id;
            do {
                id = ThreadLocalRandom.current().nextInt(100000, 300000);
            } while (usedIds.contains(id));
            WrapperPlayServerSpawnEntityLiving packet = new WrapperPlayServerSpawnEntityLiving(stand.getBukkitEntity());
            packet.setEntityID(id);
            try {
                WizardPlugin.getPlugin().getProtocolManager().sendServerPacket(player, packet.getHandle());

                shown.put(player.getUniqueId(), id);
                usedIds.add(id);
            } catch (Exception e) {
                e.printStackTrace();
            }
            stand.getBukkitEntity().remove();
        }
    }

    /**
     * Hides and stops tracking the players text.
     */
    public void hide(Player player) {
        if(shown.containsKey(player.getUniqueId())) {
            WrapperPlayServerEntityDestroy destroy = new WrapperPlayServerEntityDestroy();
            destroy.setEntityIds(new int[]{shown.get(player.getUniqueId())});

            try {
                WizardPlugin.getPlugin().getProtocolManager().sendServerPacket(player, destroy.getHandle());
                shown.remove(player.getUniqueId());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Sets and updates the private text for every player using it.
     */
    public void setText(BabelStringMessageType text, Object... params) {
        super.setText(text, params);

        shown.keySet().forEach(uuid -> changeText(Bukkit.getPlayer(uuid), text, params));
    }

    /**
     * Sets and updates the private text for an individual player.
     */
    public void changeText(Player player, BabelStringMessageType text, Object... params) {
        if(shown.containsKey(player.getUniqueId())) {
            WrapperPlayServerEntityMetadata packet = new WrapperPlayServerEntityMetadata();
            packet.setEntityID(shown.get(player.getUniqueId()));
            List<WrappedWatchableObject> lists = Lists.newArrayList();
            lists.add(new WrappedWatchableObject(0, (byte) 32));
            lists.add(new WrappedWatchableObject(2, text.toString(player, params)));
            lists.add(new WrappedWatchableObject(3, (byte) 1));
            packet.setMetadata(lists);

            try {
                WizardPlugin.getPlugin().getProtocolManager().sendServerPacket(player, packet.getHandle());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Removes the private text for every player using it.
     */
    public void remove() {
        Iterator<UUID> iterator = shown.keySet().iterator();
        while (iterator.hasNext()) {
            hide(Bukkit.getPlayer(iterator.next()));
        }
        privateText.remove(this);
    }

    /**
     * Checks if the player is in range and needs to update their text.
     */
    public void update(Player player, Location to, Location from) {
        if(shown.containsKey(player.getUniqueId())) {
            if (to.getWorld() == location.getWorld() && (from.getWorld() != location.getWorld() || location.distanceSquared(from) >= 50 * 50) && location.distanceSquared(to) <= 50 * 50) {
                hide(player);
                show(player);
            }
        }
    }

}
