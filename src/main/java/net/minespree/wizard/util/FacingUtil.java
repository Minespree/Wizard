package net.minespree.wizard.util;

import org.bukkit.Location;
import org.bukkit.block.BlockFace;

public class FacingUtil {

    // https://gist.github.com/DarkSeraphim/33a644bde86a232104d9
    public static BlockFace getFacing(Location loc) {
        float pitch = loc.getPitch();
        for(;pitch < 0; pitch += 360F);
        pitch %= 360F;
        int pitchdir = Math.round(pitch/90F) % 4;
        switch(pitchdir) {
            case 1:
                return BlockFace.UP;
            case 3:
                return BlockFace.DOWN;
            default:
                break;
        }

        float yaw = loc.getYaw();
        for(;yaw < 0; yaw += 360F);
        yaw %= 360F;
        int yawdir = Math.round(yaw / 90F) % 4;
        switch(yawdir) {
            case 0:
                return BlockFace.SOUTH;
            case 1:
                return BlockFace.WEST;
            case 2:
                return BlockFace.NORTH;
            case 3:
                return BlockFace.EAST;
        }
        return BlockFace.SELF;
    }

}
