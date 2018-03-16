package net.minespree.wizard.particle;

import com.comphenix.packetwrapper.WrapperPlayServerWorldParticles;
import com.comphenix.protocol.wrappers.EnumWrappers;
import lombok.Data;
import net.minespree.wizard.WizardPlugin;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Collection;
import java.util.Set;

@Data
public class ParticleEffect {

    private EnumWrappers.Particle particle;
    private float xOffset, yOffset, zOffset;
    private int count;
    private float speed;
    private Object data;

    public void sendParticle(Location location, Player player) {
        WrapperPlayServerWorldParticles packet = new WrapperPlayServerWorldParticles();
        packet.setParticleType(particle);
        packet.setOffsetX(xOffset);
        packet.setOffsetY(yOffset);
        packet.setOffsetZ(zOffset);
        packet.setNumberOfParticles(count);
        packet.setParticleData(speed);
        packet.setX((float) location.getX());
        packet.setY((float) location.getY());
        packet.setZ((float) location.getZ());

        try {
            WizardPlugin.getPlugin().getProtocolManager().sendServerPacket(player, packet.getHandle());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendParticle(Location location, Collection<Player> players) {
        players.forEach(player -> sendParticle(location, player));
    }

    public void sendParticle(Location location, Player... players) {
        sendParticle(location, Arrays.asList(players));
    }

    public void send(ParticleShape shape, Location origin, Player player, Object... data) {
        Set<Location> locations = get(shape, origin, data);
        for (Location loc : locations) {
            sendParticle(loc, player);
        }
    }

    public void send(ParticleShape shape, Location origin, Collection<Player> players, Object... data) {
        Set<Location> locations = get(shape, origin, data);
        for (Location loc : locations) {
            players.forEach(player -> sendParticle(loc, player));
        }
    }

    public void send(ParticleShape shape, Location origin, Object[] data, Player... players) {
        send(shape, origin, Arrays.asList(players), data);
    }

    public Set<Location> get(ParticleShape shape, Location origin, Object... data) {
        return shape.shapeFunction.apply(this, origin, data);
    }

}
