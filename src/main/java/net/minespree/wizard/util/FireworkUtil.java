package net.minespree.wizard.util;

import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.inventory.meta.FireworkMeta;

import java.util.concurrent.ThreadLocalRandom;

public class FireworkUtil {

    public static FireworkEffect randomFireworkEffect() {
        return FireworkEffect.builder()
                .with(FireworkEffect.Type.values()[ThreadLocalRandom.current().nextInt(FireworkEffect.Type.values().length)])
                .withColor(Color.fromRGB(ThreadLocalRandom.current().nextInt(255), ThreadLocalRandom.current().nextInt(255), ThreadLocalRandom.current().nextInt(255)))
                .withFade(Color.fromRGB(ThreadLocalRandom.current().nextInt(255), ThreadLocalRandom.current().nextInt(255), ThreadLocalRandom.current().nextInt(255)))
                .flicker(ThreadLocalRandom.current().nextBoolean())
                .trail(ThreadLocalRandom.current().nextBoolean())
                .build();
    }

    public static Firework randomFirework(Location location) {
        return randomFirework(location, 3,  5);
    }

    public static Firework randomFirework(Location location, int effectMax, int powerMax) {
        Firework firework = (Firework) location.getWorld().spawnEntity(location, EntityType.FIREWORK);
        FireworkMeta meta = firework.getFireworkMeta();
        for(int i = 0; i < (effectMax == 1 ? 1 : ThreadLocalRandom.current().nextInt(1, effectMax)); i++) {
            meta.addEffect(randomFireworkEffect());
        }
        meta.setPower(powerMax == 1 ? 1 : ThreadLocalRandom.current().nextInt(1, powerMax));
        firework.setFireworkMeta(meta);
        return firework;
    }

}
