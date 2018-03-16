package net.minespree.wizard.particle.effect;

import net.minespree.wizard.particle.EffectType;
import net.minespree.wizard.particle.ParticleType;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;

import java.util.Collection;
import java.util.Collections;

/**
 * @since 21/09/2017
 */
public abstract class Effect {
    protected final EffectType type;

    public final Location location;

    public Color color = null;

    public float speed = 0;

    public int period;

    public int iterations ;

    public Material material;
    public Byte materialData;

    public int particleCount = 1;

    private boolean started = false;
    private boolean done = false;
    private BukkitTask task;

    private Collection<? extends Player> viewers;

    protected JavaPlugin plugin;

    public Effect(JavaPlugin plugin, Location location, EffectType type) {
        this.plugin = plugin;
        this.location = location;
        this.type = type;
    }

    public final boolean isDone() {
        return done;
    }

    public final void start(Player viewer) {
        start(Collections.singletonList(viewer));
    }

    public final void start(Collection<? extends Player> viewers) {
        if(started)
            return;
        started = true;

        this.viewers = viewers;

        BukkitScheduler s = Bukkit.getScheduler();
        switch (type) {
            case INSTANT:
                task = s.runTaskAsynchronously(plugin, this::run);
                break;
            case REPEATING:
                task = s.runTaskTimerAsynchronously(plugin, this::run, 0, period);
                break;
        }
    }

    protected final void run() {
        if (!started || done)
            return;
        if (type == EffectType.REPEATING) {
            onRun();
            --iterations;
            if (iterations < 1)
                done();
        } else {
            done();
        }
    }

    public abstract void onRun();

    public abstract void onDone();

    public final Location getLocation() {
        return location;
    }

    protected void display(ParticleType effect, Location location) {
        display(effect, location, this.color);
    }

    protected void display(ParticleType particle, Location location, Color color) {
        display(particle, location, color, speed, particleCount);
    }

    protected void display(ParticleType particle, Location location, float speed, int amount) {
        display(particle, location, this.color, speed, amount);
    }

    protected void display(ParticleType particle, Location location, Color color, float speed, int amount) {
        particle.display(particle.getData(material, materialData), location, color, 45, 0, 0, 0, speed, amount, viewers);
    }

    private void done() {
        done = true;
        task.cancel();
        onDone();
    }
}
