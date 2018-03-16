package net.minespree.wizard.util;

import org.bukkit.util.EulerAngle;

/**
 * @since 24/09/2017
 */
public class RealEulerAngle extends EulerAngle {

    public RealEulerAngle(double x, double y, double z) {
        super(Math.toRadians(x), Math.toRadians(y), Math.toRadians(z));
    }
}
