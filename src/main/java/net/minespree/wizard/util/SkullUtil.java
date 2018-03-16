package net.minespree.wizard.util;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import net.minecraft.server.v1_8_R3.BlockPosition;
import net.minecraft.server.v1_8_R3.TileEntitySkull;
import org.apache.commons.lang.RandomStringUtils;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Skull;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.UUID;

public class SkullUtil {

    private static Field profileField;

    public static ItemStack createSkull(String skin) {
        ItemStack item = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
        setSkull(skin, true, item);
        return item;
    }

    public static ItemStack createSkullFor(Player player) {
        ItemStack item = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
        item.setItemMeta(setSkull(player, (SkullMeta) item.getItemMeta()));
        return item;
    }

    public static SkullMeta setSkull(Player player, SkullMeta meta) {
        if (profileField == null) {
            try {
                profileField = meta.getClass().getDeclaredField("profile");
                profileField.setAccessible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        try {
            profileField.set(meta, ((CraftPlayer) player).getHandle().getProfile());
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        return meta;
    }

    public static SkullMeta setSkull(String skin, boolean randomName, SkullMeta meta) {
        if (profileField == null) {
            try {
                profileField = meta.getClass().getDeclaredField("profile");
                profileField.setAccessible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        try {
            profileField.set(meta, getNonPlayerProfile(skin, randomName));
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return meta;
    }

    public static void setSkull(String skin, boolean randomName, ItemStack stack) {
        if (stack.getType() != Material.SKULL_ITEM)
            throw new IllegalArgumentException("not a skull");
        stack.setItemMeta(setSkull(skin, true, (SkullMeta) stack.getItemMeta()));
    }

    public static void setSkullWithNonPlayerProfile(String skin, boolean randomName, Block skull) {
        if(skull.getType() != Material.SKULL)
            throw new IllegalArgumentException("Block must be a skull.");
        Skull s = (Skull) skull.getState();
        try {
            setSkullProfile(s, getNonPlayerProfile(skin, randomName));
        } catch (IllegalAccessException | IllegalArgumentException | SecurityException | InvocationTargetException e) {
            e.printStackTrace();
        }
        //skull.getWorld().refreshChunk(skull.getChunk().getX(), skull.getChunk().getZ());
    }

    private static void setSkullProfile(Skull skull, GameProfile someGameprofile) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        CraftWorld world = (CraftWorld) skull.getWorld();
        TileEntitySkull tileEntitySkull = (TileEntitySkull) world.getHandle().getTileEntity(new BlockPosition(skull.getX(), skull.getY(), skull.getZ()));
        if (tileEntitySkull == null) {
            return;
        }

        tileEntitySkull.setGameProfile(someGameprofile);
    }

    private static GameProfile getNonPlayerProfile(String skin, boolean randomName) {
        GameProfile newSkinProfile = new GameProfile(UUID.randomUUID(), randomName ? RandomStringUtils.random(16, true, false) : null);
        newSkinProfile.getProperties().put("textures", new Property("textures", skin));
        return newSkinProfile;
    }

}
