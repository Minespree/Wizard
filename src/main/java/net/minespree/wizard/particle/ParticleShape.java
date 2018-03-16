package net.minespree.wizard.particle;

import net.minespree.wizard.util.TriFunction;
import net.minespree.wizard.util.VectorUtils;
import org.bukkit.Location;
import org.bukkit.util.Vector;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

public enum ParticleShape {

    CIRCLE((p, l, o) -> {
        Set<Location> locations = new TreeSet<>();
        double radius = (double) o[0];
        int amount = (int) o[1];
        double in = (2 * Math.PI) / amount;
        for (int i = 0; i < amount; i++) {
            double angle = i * in;
            locations.add(new Location(l.getWorld(), l.getX() + (radius * Math.cos(angle)), l.getY(), l.getZ() + (radius * Math.sin(angle))));
        }
        return locations;
    }),
    DOT((p, l, o) -> Collections.singleton(l)),
    BLOCK_OUTLINE((particleEffect, location, o) -> {
        Set<Location> locations = new HashSet<>();
        float a = 3F / 2F;
        double angleX, angleY;
        Vector vector = new Vector();

        int particleamount = (int) o[1];

        for (int i = 0; i < 4; i++) {
            angleY = i * Math.PI / 2;
            for (int j = 0; j < 2; j++) {
                angleX = j * Math.PI;
                for (int p = 0; p <= particleamount; p++) {
                    vector.setX(a).setY(a);
                    vector.setZ(3F * p / particleamount - a);
                    VectorUtils.rotateAroundAxisX(vector, angleX);
                    VectorUtils.rotateAroundAxisY(vector, angleY);

                    locations.add(location.add(vector));
                    location.subtract(vector);
                }
            }

            for (int p = 0; p <= particleamount; p++) {
                vector.setX(a).setZ(a);
                vector.setY(3F * p / particleamount - a);
                VectorUtils.rotateAroundAxisY(vector, angleY);

                locations.add(location.add(vector));
                location.subtract(vector);
            }
        }
        return locations;
    });

    TriFunction<ParticleEffect, Location, Object[], Set<Location>> shapeFunction;

    ParticleShape(TriFunction<ParticleEffect, Location, Object[], Set<Location>> shapeFunction) {
        this.shapeFunction = shapeFunction;
    }

}
