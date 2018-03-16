package net.minespree.wizard.util;

import com.google.common.base.Preconditions;
import lombok.experimental.UtilityClass;
import org.bukkit.Bukkit;
import org.bukkit.Difficulty;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.entity.Player;

@UtilityClass
public class SetupUtil {
    /**
     * Sets some sensible defaults on the world.
     * @param world the world to set defaults on
     */
    public static void setupWorld(World world) {
        Preconditions.checkNotNull(world, "world");
        world.setGameRuleValue("doDaylightCycle", "false");
        world.setGameRuleValue("doFireTick", "false");
        world.setGameRuleValue("doMobSpawning", "false");
        world.setGameRuleValue("doWeatherCycle", "false");
        world.setGameRuleValue("randomTickSpeed", "0");
        world.setGameRuleValue("announceAdvancements", "false");
        world.setDifficulty(Difficulty.NORMAL);
        world.setTime(0);
        world.setStorm(false);
    }

    public static void setupPlayer(Player player) {
        setupPlayer(player, true);
    }

    /**
     * Sets up a sane, near-vanilla setup for a player.
     * @param player the player to use
     */
    public static void setupPlayer(Player player, boolean scoreboardNew) {
        Preconditions.checkNotNull(player);

        // Heal the player.
        player.setHealth(player.getMaxHealth());
        player.setFoodLevel(20);
        player.setSaturation(20);
        player.setFireTicks(0);
        player.setWalkSpeed(0.2f);

        // Remove addEffect effects.
        player.getActivePotionEffects().forEach(e -> player.removePotionEffect(e.getType()));

        // Clear armor and inventory.
        player.getInventory().clear();
        player.getInventory().setArmorContents(null);
        player.getInventory().setHeldItemSlot(0);

        // Clear fancy stuff.
        player.setExp(0);
        player.setLevel(0);
        player.setGameMode(GameMode.SURVIVAL);
        if(scoreboardNew)
            player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
    }
}
