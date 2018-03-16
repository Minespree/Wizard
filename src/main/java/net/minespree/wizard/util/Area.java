package net.minespree.wizard.util;

import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;

import java.util.List;

public class Area {

    @Getter @Setter
    private double xMin, xMax, yMin, yMax, zMin, zMax;

    public Area(Location centre, double xWidth, double yHeight, double zWidth) {
        this(centre.getX() + xWidth, centre.getX() - xWidth, centre.getY() + yHeight, centre.getY() - yHeight, centre.getZ() + zWidth, centre.getZ() - zWidth);
    }

    public Area(Location centre, double xWidth, double zWidth) {
        this(centre.getX() + xWidth, centre.getX() - xWidth, centre.getY(), centre.getY(), centre.getZ() + zWidth, centre.getZ() - zWidth);
    }

    public Area(ConfigurationSection pos1, ConfigurationSection pos2) {
        this(pos1.getDouble("X"), pos2.getDouble("X"), pos1.getDouble("Y"),
                pos2.getDouble("Y"),pos1.getDouble("Z"), pos2.getDouble("Z"));
    }

    public Area(Location pos1, Location pos2) {
        this(pos1.getX(), pos2.getX(), pos1.getY(), pos2.getY(), pos1.getZ(), pos2.getZ());
    }

    public Area(double x1, double x2, double y1, double y2, double z1, double z2) {
        this.xMin = Math.min(x1, x2);
        this.xMax = Math.max(x1, x2);
        this.yMin = Math.min(y1, y2);
        this.yMax = Math.max(y1, y2);
        this.zMin = Math.min(z1, z2);
        this.zMax = Math.max(z1, z2);
    }

    public void increase(int size, boolean y) {
        if(y) {
            yMin -= size;
            yMax += size;
        }
        xMin -= size;
        xMax += size;
        zMin -= size;
        zMax += size;
    }

    public boolean intersects(Area area) {
        return area.getXMin() <= xMax && area.getXMax() >= xMin && area.getYMin() <= yMax && area.getYMax() >= yMin && area.getZMin() <= zMax && area.getZMax() >= zMin;
    }

    public boolean inside(Area area) {
        return xMin >= area.getXMin() && xMax <= area.getXMax() && yMin >= area.getYMin() && yMax <= area.getYMax() && zMin >= area.getZMin() && zMax <= area.getZMax();
    }

    public boolean inside(Location location, boolean includeY) {
        return inside(location.getX(), location.getY(), location.getZ(), includeY);
    }

    public boolean inside(Location location) {
        return inside(location, true);
    }

    public boolean inside(double x, double y, double z, boolean includeY) {
        return x >= xMin && x <= xMax && (!includeY || (y >= yMin && y <= yMax)) && z >= zMin && z <= zMax;
    }

    public boolean inside(double x, double y, double z) {
        return inside(x, y, z, true);
    }

    public List<Block> getBlocks(World world, boolean ignoreY) {
        List<Block> blocks = Lists.newArrayList();
        if(!ignoreY) {
            for (double y = yMin; y <= yMax; y++) {
                for (double x = xMin; x <= xMax; x++) {
                    for (double z = zMin; z <= zMax; z++) {
                        blocks.add(world.getBlockAt((int) x, (int) y, (int) z));
                    }
                }
            }
        } else {
            for (double x = xMin; x <= xMax; x++) {
                for (double z = zMin; z <= zMax; z++) {
                    blocks.add(world.getBlockAt((int) x, (int) yMax, (int) z));
                }
            }
        }
        return blocks;
    }

    public Location centre(World world) {
        return new Location(world, (getXMin() + getXMax()) / 2.0, (getYMin() + getYMax()) / 2.0, (getZMin() + getZMax()) / 2.0, 0.0F, 0.0F);
    }

    public Location randomLocation(World world, float yaw, float pitch) {
        return randomLocation(world, false, yaw, pitch);
    }

    public Location randomLocation(World world) {
        return randomLocation(world, false, 0.0F, 0.0F);
    }

    public Location randomLocation(World world, boolean y) {
        return randomLocation(world, y, 0.0F, 0.0F);
    }

    public Location randomLocation(World world, boolean y, float yaw, float pitch) {
        return new Location(world, getXMin() + (getXMax() - getXMin()) * Math.random(), y ? getYMin() + (getYMax() - getYMin()) * Math.random() : getYMin(), getZMin() + (getZMax() - getZMin()) * Math.random(), yaw, pitch);
    }


}
