package net.minespree.wizard.gui;

import com.comphenix.packetwrapper.WrapperPlayClientUseEntity;
import com.comphenix.packetwrapper.WrapperPlayServerEntityDestroy;
import com.comphenix.packetwrapper.WrapperPlayServerEntityEquipment;
import com.comphenix.packetwrapper.WrapperPlayServerSpawnEntityLiving;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.primitives.Ints;
import net.minecraft.server.v1_8_R3.EntityArmorStand;
import net.minespree.wizard.WizardPlugin;
import net.minespree.wizard.util.RealEulerAngle;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;

/**
 * @since 23/09/2017
 */
public class InteractableMenu {

    /**
     * TODO: animate?
     */

    private Player player;

    private int anglePerButton = 35;
    private List<MenuButton> buttonList = Lists.newArrayList();
    private Multimap<Integer, MenuButton> multiButtonList = ArrayListMultimap.create();

    private Map<Integer, Consumer<Player>> actions = Maps.newHashMap();

    private Map<Block, Integer> mappings = Maps.newHashMap();

    private boolean closeOnInteract = true;
    private boolean smallArmorStands = false;
    private int radius = 3;

    private String subtitle;

    private BukkitTask traceTask;
    private ProtocolManager protocolManager;

    private int animateId;

    private boolean multiLine = false;

    public InteractableMenu(Player player) {
        this.player = player;
        this.protocolManager = ProtocolLibrary.getProtocolManager();
    }

    public InteractableMenu setMultiLine(boolean multiLine) {
        this.multiLine = multiLine;
        return this;
    }

    public InteractableMenu setCloseOnInteract(boolean closeOnInteract) {
        this.closeOnInteract = closeOnInteract;
        return this;
    }

    public InteractableMenu setSmall(boolean small) {
        this.smallArmorStands = small;
        return this;
    }

    public InteractableMenu setRadius(int radius) {
        this.radius = radius;
        return this;
    }

    public InteractableMenu addButton(ItemStack stack, boolean verticalGrip, String text, Consumer<Player> click) {
        if (multiLine) throw new IllegalArgumentException("buttons can only be created from addButton(Integer, ...)");

        buttonList.add(new MenuButton(stack, verticalGrip, text, click));
        return this;
    }

    public InteractableMenu addButton(int line, ItemStack stack, boolean verticalGrip, String text, Consumer<Player> click) {
        if (!multiLine) throw new IllegalArgumentException("not multiline");

        multiButtonList.put(line, new MenuButton(stack, verticalGrip, text, click));
        return this;
    }

    public InteractableMenu setSubtitle(String subtitle) {
        this.subtitle = subtitle;
        return this;
    }

    public void build() {
        if (multiLine) throw new IllegalArgumentException("multiline can only be built from #buildMulti");

        List<Location> semiCircle = getFocusedSemiCircle(player, this.radius);

        addCloseButton(semiCircle.get(0)); // first
        addCloseButton(semiCircle.get(semiCircle.size() - 1)); // last

        int currentIndex = 40;

        if (buttonList.size() == 1) {
            currentIndex = 89; // center.
        } else if (buttonList.size() == 2) {
            currentIndex = 40;
        } else if (buttonList.size() == 3) {
            currentIndex = 20;
            anglePerButton = 40;
        } else if (buttonList.size() > 3) {
            currentIndex = 10;
            anglePerButton = 35;
        }

        for (MenuButton button : buttonList) {
            createButton(button, lookAt(semiCircle.get(currentIndex += anglePerButton), player.getLocation()));
        }

        createSubtitle(semiCircle.get(89));
        registerListener();

//        traceTask = Bukkit.getScheduler().runTaskTimer(Stands.get(), () -> {
//            if (player == null || !player.isOnline()) {
//                traceTask.cancel();
//                return;
//            }
//
//            RayTrace trace = new RayTrace(player.getEyeLocation().toVector(), player.getEyeLocation().getDirection());
//            ArrayList<Vector> positions = trace.traverse(Math.pow(radius, 2), 0.01);
//            for (Vector vector : positions) {
//                Location position = vector.toLocation(player.getWorld());
//                Block block = player.getWorld().getBlockAt(position);
//
//                if (block == null) continue;
//
//                if (trace.intersects(new BoundingBox(block), radius, 0.01) && mappings.containsKey(block)) {
//                    int id = mappings.get(block);
//                    if (animateId == -1 || animateId != id) {
//                        animateId = id;
//
//                        // TODO: Animate? idk.
//                    }
//                } else {
//                    animateId = -1;
//                }
//            }
//        }, 0L, 10L);
    }

    public void buildMulti() {
        if (!multiLine) throw new IllegalArgumentException("not multiline");

        for (int line : multiButtonList.keys()) {
            List<Location> semiCircle = getFocusedSemiCircle(player, this.radius);
            List<MenuButton> buttons = new ArrayList<>(multiButtonList.get(line));

            if (line == 1) {
                addCloseButton(semiCircle.get(0));
                addCloseButton(semiCircle.get(semiCircle.size() - 1));

                createSubtitle(semiCircle.get(89));
            } else {
                semiCircle.forEach(location -> location.add(0, smallArmorStands ? 1 : 2, 0));
            }

            int currentIndex = 40;

            if (buttons.size() == 1) {
                currentIndex = 89; // center.
            } else if (buttons.size() == 2) {
                currentIndex = 40;
            } else if (buttons.size() == 3) {
                currentIndex = 20;
                anglePerButton = 40;
            } else if (buttons.size() > 3) {
                currentIndex = 10;
                anglePerButton = 35;
            }

            for (MenuButton button : buttons) {
                createButton(button, lookAt(semiCircle.get(currentIndex += anglePerButton), player.getLocation()));
            }
        }

        registerListener();
    }

    private void registerListener() {
        protocolManager.addPacketListener(new PacketAdapter(WizardPlugin.getPlugin(), PacketType.Play.Client.USE_ENTITY) {
            @Override
            public void onPacketReceiving(PacketEvent event) {
                if (event.getPlayer() == player) {
                    WrapperPlayClientUseEntity entity = new WrapperPlayClientUseEntity(event.getPacket());
                    if (actions.containsKey(entity.getTargetID())) {
                        Optional.ofNullable(actions.getOrDefault(entity.getTargetID(), null)).ifPresent(c -> {
                            c.accept(event.getPlayer());
                            protocolManager.removePacketListener(this);
                            if (closeOnInteract) close();
                        });
                    }
                }
            }
        });
    }

    private void createSubtitle(Location location) {
        if (subtitle != null && !subtitle.isEmpty()) {
            EntityArmorStand stand = new EntityArmorStand(((CraftWorld) location.getWorld()).getHandle());
            stand.setPositionRotation(location.getX(), location.getY() - 1.5, location.getZ(), location.getYaw(), location.getPitch());
            stand.setCustomName(subtitle);
            stand.setCustomNameVisible(true);
            stand.setInvisible(true);
            stand.setGravity(false);
            stand.setBasePlate(false);

            int id;
            do {
                id = ThreadLocalRandom.current().nextInt(100000, 300000);
            } while (actions.containsKey(id));

            WrapperPlayServerSpawnEntityLiving packet = new WrapperPlayServerSpawnEntityLiving(stand.getBukkitEntity());
            packet.setEntityID(id);

            actions.put(id, null);

            try {
                protocolManager.sendServerPacket(player, packet.getHandle());
            } catch (Exception e) {
                e.printStackTrace();
            }

            stand.getBukkitEntity().remove();
        }
    }

    private void createButton(MenuButton button, Location location) {
        EntityArmorStand stand = new EntityArmorStand(((CraftWorld) location.getWorld()).getHandle());
        stand.setPositionRotation(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
        if (button.getText() != null && !button.getText().isEmpty()) {
            stand.setCustomName(button.getText());
            stand.setCustomNameVisible(true);
        }
        stand.setInvisible(true);
        stand.setGravity(false);
        stand.setBasePlate(false);
        stand.setSmall(smallArmorStands);

        int id;
        do {
            id = ThreadLocalRandom.current().nextInt(100000, 300000);
        } while (actions.containsKey(id));

        WrapperPlayServerSpawnEntityLiving packet = new WrapperPlayServerSpawnEntityLiving(stand.getBukkitEntity());
        packet.setEntityID(id);

        WrapperPlayServerEntityEquipment equipment = new WrapperPlayServerEntityEquipment();
        equipment.setEntityID(id);

        if (button.isVerticalGrip()) {
            equipment.setSlot(0); // 0 = held item
            equipment.setItem(button.getStack());
        } else {
            equipment.setSlot(4); // 4 = helmet
            equipment.setItem(button.getStack());
        }

        try {
            protocolManager.sendServerPacket(player, packet.getHandle());
            protocolManager.sendServerPacket(player, equipment.getHandle());
            if (button.isVerticalGrip()) {
                ((ArmorStand) stand.getBukkitEntity()).setRightArmPose(new RealEulerAngle(180, 90, 0));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        stand.getBukkitEntity().remove();

        actions.put(id, button.getClick());
        location.getBlock();
        Block block;
        if (smallArmorStands) {
            block = location.add(0, 0.5, 0).getBlock();
        } else {
            block = location.add(0, 1.5, 0).getBlock();
        }
        mappings.put(block, id);
    }

    private void addCloseButton(Location location) {
        createButton(new MenuButton(new ItemStack(Material.REDSTONE_BLOCK), false, ChatColor.RED + "Close", player -> close()), location);
    }

    public void close() {
        int[] destroy = Ints.toArray(actions.keySet());

        WrapperPlayServerEntityDestroy packet = new WrapperPlayServerEntityDestroy();
        packet.setEntityIds(destroy);

        try {
            protocolManager.sendServerPacket(player, packet.getHandle());
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (traceTask != null) {
            traceTask.cancel();
            traceTask = null;
        }
    }

    private Vector control = new Vector(0, 0,1);

    private List<Location> getFocusedSemiCircle(Player focus, double radius) {
        List<Location> locations = Lists.newArrayList();
        Vector v = focus.getLocation().getDirection().setY(0).normalize();
        double angle = v.angle(control);

        double oneDegInRad = Math.PI / 180;
        for (double rad = angle - (Math.PI / 2); rad < angle + (Math.PI / 2); rad += oneDegInRad) {
            double cos = Math.cos(rad);
            double sin = Math.sin(rad);

            Location location = focus.getLocation().add(sin * radius, 0, cos * radius);
            locations.add(location);
        }

        Collections.reverse(locations);
        return locations;
    }

    private Location lookAt(Location loc, Location lookAt) {
        loc = loc.clone();
        double dx = lookAt.getX() - loc.getX();
        double dy = lookAt.getY() - loc.getY();
        double dz = lookAt.getZ() - loc.getZ();
        if (dx != 0) {
            if (dx < 0) {
                loc.setYaw((float) (1.5 * Math.PI));
            } else {
                loc.setYaw((float) (0.5 * Math.PI));
            }
            loc.setYaw(loc.getYaw() - (float) Math.atan(dz / dx));
        } else if (dz < 0) {
            loc.setYaw((float) Math.PI);
        }
        double dxz = Math.sqrt(Math.pow(dx, 2) + Math.pow(dz, 2));
        loc.setPitch((float) -Math.atan(dy / dxz));
        loc.setYaw(-loc.getYaw() * 180f / (float) Math.PI);
        loc.setPitch(loc.getPitch() * 180f / (float) Math.PI);
        return loc;
    }

    public class MenuButton {
        private final ItemStack stack;
        private final boolean verticalGrip;
        private final String text;
        private final Consumer<Player> click;

        MenuButton(ItemStack stack, boolean verticalGrip, String text, Consumer<Player> click) {
            this.stack = stack;
            this.verticalGrip = verticalGrip;
            this.text = text;
            this.click = click;
        }

        ItemStack getStack() {
            return stack;
        }

        boolean isVerticalGrip() {
            return verticalGrip;
        }

        String getText() {
            return text;
        }

        Consumer<Player> getClick() {
            return click;
        }
    }
}
