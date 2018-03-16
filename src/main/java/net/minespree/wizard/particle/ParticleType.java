package net.minespree.wizard.particle;

import com.comphenix.packetwrapper.WrapperPlayServerWorldParticles;
import com.comphenix.protocol.wrappers.EnumWrappers;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.Collection;

/**
 * @since 21/09/2017
 */
public enum ParticleType {
    EXPLOSION_NORMAL(0),
    EXPLOSION_LARGE(1),
    EXPLOSION_HUGE(2),
    FIREWORKS_SPARK(3),
    WATER_BUBBLE(4, false, true),
    WATER_SPLASH(5),
    WATER_WAKE(6),
    SUSPENDED(7, false, true),
    SUSPENDED_DEPTH(8),
    CRIT(9),
    CRIT_MAGIC(10),
    SMOKE_NORMAL(11),
    SMOKE_LARGE(12),
    SPELL(13),
    SPELL_INSTANT(14),
    SPELL_MOB(15),
    SPELL_MOB_AMBIENT(16),
    SPELL_WITCH(17),
    DRIP_WATER(18),
    DRIP_LAVA(19),
    VILLAGER_ANGRY(20),
    VILLAGER_HAPPY(21),
    TOWN_AURA(22),
    NOTE(23),
    PORTAL(24),
    ENCHANTMENT_TABLE(25),
    FLAME(26),
    LAVA(27),
    FOOTSTEP(28),
    CLOUD(29),
    REDSTONE(30),
    SNOWBALL(31),
    SNOW_SHOVEL(32),
    SLIME(33),
    HEART(34),
    BARRIER(35),
    ITEM_CRACK(36, true),
    BLOCK_CRACK(37, true),
    BLOCK_DUST(38, true),
    WATER_DROP(39),
    ITEM_TAKE(40),
    MOB_APPEARANCE(41),
    DRAGON_BREATH(42),
    END_ROD(43),
    DAMAGE_INDICATOR(44),
    SWEEP_ATTACK(45);

    private final int id;
    private final boolean requiresData;
    private final boolean requiresWater;

    ParticleType(int id, boolean requiresData, boolean requiresWater) {
        this.id = id;
        this.requiresData = requiresData;
        this.requiresWater = requiresWater;
    }

    ParticleType(int id, boolean requiresData) {
        this(id, requiresData, false);
    }

    ParticleType(int id) {
        this(id, false);
    }

    private static boolean isWater(Location location) {
        Material material = location.getBlock().getType();
        return material == Material.WATER || material == Material.STATIONARY_WATER;
    }

    private static boolean isDataCorrect(ParticleType effect, ParticleData data) {
        return ((effect == BLOCK_CRACK || effect == BLOCK_DUST) && data instanceof BlockData) || effect == ITEM_CRACK && data instanceof ItemData;
    }

    public static abstract class ParticleData {
        private final Material material;
        private final byte data;
        private final int[] packetData;

        @SuppressWarnings("deprecation")
        public ParticleData(Material material, byte data) {
            this.material = material;
            this.data = data;
            this.packetData = new int[]{(data << 12) | (material.getId() & 4095)};
        }

        public Material getMaterial() {
            return material;
        }

        public byte getData() {
            return data;
        }

        public int[] getPacketData() {
            return packetData;
        }
    }

    public static final class ItemData extends ParticleData {
        public ItemData(Material material, byte data) {
            super(material, data);
        }
    }

    public static final class BlockData extends ParticleData {
        public BlockData(Material material, byte data) throws IllegalArgumentException {
            super(material, data);
            if (!material.isBlock()) {
                throw new IllegalArgumentException("The material is not a block");
            }
        }
    }

    private static final class ParticleDataException extends RuntimeException {
        private static final long serialVersionUID = 3203085387160737484L;

        public ParticleDataException(String message) {
            super(message);
        }
    }
    public void display(ParticleData data, Location center, Color color, double range, float offsetX, float offsetY, float offsetZ, float speed, int amount, Collection<? extends Player> players) {
        if (color != null && (this == ParticleType.REDSTONE || this == ParticleType.SPELL_MOB || this == ParticleType.SPELL_MOB_AMBIENT)) {
            amount = 0;
            if (speed == 0) {
                speed = 1;
            }
            offsetX = (float) color.getRed() / 255;
            offsetY = (float) color.getGreen() / 255;
            offsetZ = (float) color.getBlue() / 255;

            if (offsetX < Float.MIN_NORMAL) {
                offsetX = Float.MIN_NORMAL;
            }
        }

        if(requiresData) {
            if(data == null) {
                System.err.println("Particle " + name() + " requires data");
                return;
            } if(! isDataCorrect(this, data)) {
                throw new ParticleDataException("The particle data type is incorrect: " + data + " for " + this);
            }
        }

        WrapperPlayServerWorldParticles wrapper = new WrapperPlayServerWorldParticles();
        wrapper.setParticleType(EnumWrappers.Particle.getById(id));
        wrapper.setLongDistance(range > 16);
        wrapper.setX((float) center.getX());
        wrapper.setY((float) center.getY());
        wrapper.setZ((float) center.getZ());
        wrapper.setOffsetX(offsetX);
        wrapper.setOffsetY(offsetY);
        wrapper.setOffsetZ(offsetZ);
        wrapper.setParticleData(speed);
        wrapper.setNumberOfParticles(amount);
        if(data != null)
            wrapper.setData(data.getPacketData());

        players.forEach(wrapper::sendPacket);
    }

    public ParticleData getData(Material material, Byte blockData) {
        ParticleData data = null;
        if (blockData == null) {
            blockData = 0;
        }
        if (this == ParticleType.BLOCK_CRACK || this == ParticleType.ITEM_CRACK || this == ParticleType.BLOCK_DUST) {
            if (material != null && material != Material.AIR) {
                if (this == ParticleType.ITEM_CRACK) {
                    data = new ParticleType.ItemData(material, blockData);
                } else {
                    data = new ParticleType.BlockData(material, blockData);
                }
            }
        }

        return data;
    }
}
