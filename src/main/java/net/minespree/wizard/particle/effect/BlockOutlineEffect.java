package net.minespree.wizard.particle.effect;

import net.minespree.wizard.particle.EffectType;
import net.minespree.wizard.particle.ParticleType;
import net.minespree.wizard.util.VectorUtils;
import org.bukkit.Location;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

/**
 * @since 21/09/2017
 */
public class BlockOutlineEffect extends Effect {
    public final ParticleType particle;

    public float edgeLength = 3;

    public int particles = 8;

    protected int step = 0;

    public BlockOutlineEffect(JavaPlugin plugin, ParticleType particle, Location location) {
        super(plugin, location, EffectType.REPEATING);
        this.particle = particle;
        period = 5;
        iterations = 200;
    }

    @Override
    public void onRun() {
        Location location = getLocation();
        drawCubeOutline(location);
        step++;
    }

    private void drawCubeOutline(Location location) {
        float a = edgeLength / 2;
        double angleX, angleY;
        Vector v = new Vector();
        for (int i = 0; i < 4; i++) {
            angleY = i * Math.PI / 2;
            for (int j = 0; j < 2; j++) {
                angleX = j * Math.PI;
                for (int p = 0; p <= particles; p++) {
                    v.setX(a).setY(a);
                    v.setZ(edgeLength * p / particles - a);
                    VectorUtils.rotateAroundAxisX(v, angleX);
                    VectorUtils.rotateAroundAxisY(v, angleY);

                    display(particle, location.add(v));
                    location.subtract(v);
                }
            }
            for (int p = 0; p <= particles; p++) {
                v.setX(a).setZ(a);
                v.setY(edgeLength * p / particles - a);
                VectorUtils.rotateAroundAxisY(v, angleY);

                display(particle, location.add(v));
                location.subtract(v);
            }
        }
    }

    @Override
    public void onDone() {

    }
}
