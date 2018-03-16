package net.minespree.wizard.util;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import lombok.Data;
import lombok.Getter;
import lombok.NonNull;
import net.minespree.babel.Babel;
import net.minespree.babel.BabelMessageType;
import net.minespree.babel.BabelMultiMessageType;
import net.minespree.babel.BabelStringMessageType;
import org.bson.Document;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;
import java.util.stream.Collectors;

@Getter
public class ItemBuilder implements Cloneable {

    private Material material;
    private int amount;
    private short durability;
    private BabelData displayName;
    private List<BabelData> lore = Lists.newArrayList();
    private Map<Enchantment, Integer> enchantments = Maps.newHashMap();
    private Set<ItemFlag> flags = Sets.newHashSet();
    private String owner;
    private String skinData;
    private boolean perPlayerOwner;
    private boolean unbreakable;
    private Color colour;
    private List<PotionEffect> effects = Lists.newLinkedList();
    private Potion potion;

    public ItemBuilder(Material material, int amount, short durability) {
        this.material = material;
        this.amount = amount;
        this.durability = durability;
    }

    public ItemBuilder(ItemStack item) {
        this.material = item.getType();
        this.amount = item.getAmount();
        this.durability = item.getDurability();
        if(item.hasItemMeta()) {
            ItemMeta meta = item.getItemMeta();
            if(meta.hasDisplayName()) {
                this.displayName = new BabelData(Babel.messageStatic(meta.getDisplayName()), null);
            }
            if(meta.hasLore()) {
                this.lore = meta.getLore().stream().map(s -> new BabelData(Babel.messageStatic(s), null)).collect(Collectors.toList());
            }
            if(!meta.getItemFlags().isEmpty()) {
                this.flags = Sets.newHashSet(meta.getItemFlags());
            } else this.flags = Sets.newHashSet();
            if(!meta.getEnchants().isEmpty()) {
                this.enchantments = Maps.newHashMap(meta.getEnchants());
            } else this.enchantments = Maps.newHashMap();
            this.unbreakable = meta.spigot().isUnbreakable();
            if(meta instanceof SkullMeta) {
                SkullMeta skullMeta = (SkullMeta) meta;
                if(skullMeta.hasOwner()) {
                    this.owner = skullMeta.getOwner();
                }
            } else if(meta instanceof LeatherArmorMeta) {
                this.colour = ((LeatherArmorMeta) meta).getColor();
            } else if(meta instanceof PotionMeta) {
                PotionMeta potionMeta = (PotionMeta) meta;
                this.effects = Lists.newArrayList(potionMeta.getCustomEffects());
            }
        }
    }

    public ItemBuilder(Document document) {
        try {
            material(Material.valueOf(document.getString("material").toUpperCase()));
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Failed to find Material" + (document.containsKey("material") ? " named " + document.getString("material").toUpperCase() : ""));
        }
        if (document.containsKey("amount"))
            amount(document.getInteger("amount"));
        else amount(1);
        if (document.containsKey("durability"))
            durability(document.getInteger("durability").shortValue());
        if (document.containsKey("enchantments")) {
            List<Document> enchantments = (List<Document>) document.get("enchantments");
            for (Document enchantment : enchantments) {
                enchant(Enchantment.getByName(enchantment.getString("enchant").toUpperCase()), enchantment.getInteger("level"));
            }
        }
        if (document.containsKey("babelName"))
            displayName(Babel.translate(document.getString("babelName")));
        if (document.containsKey("name"))
            displayName(ChatColor.translateAlternateColorCodes('&', document.getString("name")));
        if (document.containsKey("babelLore"))
            lore(Babel.translateMulti(document.getString("babelLore")));
        if (document.containsKey("singleBabelLore"))
            lore(Babel.translate(document.getString("singleBabelLore")));
        if (document.containsKey("lore"))
            lore((List<String>) document.get("lore"));
        if (document.containsKey("flags")) {
            for (String flag : ((List<String>) document.get("flags"))) {
                addItemFlags(ItemFlag.valueOf(flag.toUpperCase()));
            }
        }
        if (document.containsKey("skinData")) {
            skinData(document.getString("skinData"));
        }
        if (document.containsKey("skullOwner")) {
            owner(document.getString("skullOwner"));
        }
        if(document.containsKey("colour") && material.name().startsWith("LEATHER_")) {
            Document colourDoc = (Document) document.get("colour");
            colour(Color.fromRGB(colourDoc.getInteger("r"), colourDoc.getInteger("g"), colourDoc.getInteger("b")));
        }
    }

    public ItemBuilder(ConfigurationSection section) {
        if(section.contains("id")) {
            material(Material.getMaterial(section.getInt("id")));
        }
        if(section.contains("data")) {
            durability((short) section.getInt("data"));
        }
        if(section.contains("name")) {
            displayName(section.getString("name"));
        }
        if(section.contains("amount")) {
            amount(section.getInt("amount"));
        } else amount(1);
        if(section.contains("lore")) {
            lore(section.getStringList("lore"));
        }
        if(section.contains("flags")) {
            flags(section.getStringList("flags").stream().map(flag -> ItemFlag.valueOf(flag.toUpperCase())).collect(Collectors.toSet()));
        }
        if(section.contains("enchantments")) {
            for (String enchant : section.getStringList("enchantments")) {
                String[] data = enchant.split(":");
                enchant(Enchantment.getById(Integer.parseInt(data[0])), Integer.parseInt(data[1]));
            }
        }
        if(section.contains("unbreakable")) {
            unbreakable(section.getBoolean("unbreakable"));
        }
        if(section.contains("potion")) {
            ConfigurationSection potionSection = section.getConfigurationSection("potion");
            for (String potion : potionSection.getKeys(false)) {
                addEffect(new PotionEffect(PotionEffectType.getByName(potion), potionSection.getInt(potion + ".duration"), potionSection.getInt(potion + ".amplifier")));
            }
        }
    }

    public ItemBuilder(Material material, int amount) {
        this(material, amount, (short) 0);
    }

    public ItemBuilder(Material material) {
        this(material, 1, (short) 0);
    }

    public ItemBuilder material(Material material) {
        this.material = material;
        return this;
    }

    public ItemBuilder amount(int amount) {
        this.amount = amount;
        return this;
    }

    public ItemBuilder durability(short durability) {
        this.durability = durability;
        return this;
    }

    public ItemBuilder displayName(BabelStringMessageType displayName, Object... params) {
        this.displayName = new BabelData(displayName, params);
        return this;
    }

    public ItemBuilder displayName(String displayName, Object... params) {
        this.displayName = new BabelData(Babel.messageStatic(displayName), params);
        return this;
    }

    public ItemBuilder lore(BabelMessageType line, Object... data) {
        this.lore.add(new BabelData(line, data));
        return this;
    }

    public ItemBuilder lore(String line, Object... data) {
        lore(Babel.messageStatic(line), data);
        return this;
    }

    public ItemBuilder lore(List<String> lore) {
        this.lore = lore.stream().map(s -> new BabelData(Babel.messageStatic(s), null)).collect(Collectors.toList());
        return this;
    }

    public ItemBuilder clearLore() {
        lore.clear();
        return this;
    }

    public ItemBuilder enchant(Enchantment enchantment, int level) {
        enchantments.put(enchantment, level);
        return this;
    }

    public ItemBuilder unenchant(Enchantment enchantment) {
        enchantments.remove(enchantment);
        return this;
    }

    public ItemBuilder clearEnchants() {
        enchantments.clear();
        return this;
    }

    public ItemBuilder flags(Set<ItemFlag> flags) {
        this.flags = flags;
        return this;
    }

    public ItemBuilder addItemFlags(ItemFlag... flags) {
        this.flags.addAll(Arrays.asList(flags));
        return this;
    }

    public ItemBuilder removeItemFlags(ItemFlag... flags) {
        this.flags.removeAll(Arrays.asList(flags));
        return this;
    }

    public ItemBuilder clearItemFlags() {
        flags.clear();
        return this;
    }

    public ItemBuilder owner(String owner) {
        this.owner = owner;
        return this;
    }

    public ItemBuilder skinData(String skinData) {
        this.skinData = skinData;
        return this;
    }

    public ItemBuilder perPlayerOwner(boolean perPlayerOwner) {
        this.perPlayerOwner = perPlayerOwner;
        return this;
    }

    public ItemBuilder unbreakable(boolean unbreakable) {
        this.unbreakable = unbreakable;
        return this;
    }

    public ItemBuilder colour(Color colour) {
        this.colour = colour;
        return this;
    }

    public ItemBuilder colour(int r, int g, int b) {
        this.colour = Color.fromRGB(r, g, b);
        return this;
    }

    public ItemBuilder glow() {
        enchant(Enchantment.DURABILITY, 1);
        addItemFlags(ItemFlag.HIDE_ENCHANTS);
        return this;
    }

    public ItemBuilder unglow() {
        unenchant(Enchantment.DURABILITY);
        removeItemFlags(ItemFlag.HIDE_ENCHANTS);
        return this;
    }

    public ItemBuilder addEffect(PotionEffect effect) {
        effects.add(effect);
        return this;
    }

    public ItemBuilder potion(Potion potion) {
        this.potion = potion;
        return this;
    }

    public ItemStack build(Player player) {
        ItemStack item = new ItemStack(material);
        item.setAmount(amount);
        item.setDurability(durability);
        ItemMeta meta = item.getItemMeta();

        if(displayName != null) {
            meta.setDisplayName((String) displayName.getType().toString(player, displayName.getParams()));
        }

        if(!lore.isEmpty()) {
            List<String> nLore = new ArrayList<>();

            for (BabelData data : lore) {
                if(data.getType() instanceof BabelMultiMessageType) {
                    nLore.addAll((List<String>) data.getType().toString(player, data.getParams()));
                } else if(data.getType() instanceof BabelStringMessageType) {
                    nLore.add((String) data.getType().toString(player, data.getParams()));
                }
            }
            meta.setLore(nLore);
        }
        if(!enchantments.isEmpty()) {
            enchantments.forEach((item::addUnsafeEnchantment));
        }
        if(!flags.isEmpty())
            flags.forEach(meta::addItemFlags);
        if(meta instanceof SkullMeta) {
            SkullMeta skullMeta = (SkullMeta) meta;
            if(owner != null)
                skullMeta.setOwner(owner);
            if(perPlayerOwner && player != null)
                skullMeta.setOwner(player.getName());
            if(skinData != null)
                SkullUtil.setSkull(skinData, true, skullMeta);
        } else if(meta instanceof PotionMeta) {
            if(potion != null) {
                potion.apply(item);
            }
            if(!effects.isEmpty()) {
                PotionMeta potionMeta = (PotionMeta) meta;
                potionMeta.clearCustomEffects();
                effects.forEach(effect -> potionMeta.addCustomEffect(effect, true));
            }
        }
        if(unbreakable)
            meta.spigot().setUnbreakable(unbreakable);
        item.setItemMeta(meta);
        return item;
    }

    public ItemStack build() {
        return build(null);
    }

    @Override
    public ItemBuilder clone() {
        ItemBuilder builder = new ItemBuilder(material, amount, durability);
        if(displayName != null)
            builder.displayName = new BabelData(displayName.type, displayName.params);
        if(!lore.isEmpty())
            builder.lore = Lists.newArrayList(lore);
        if(!enchantments.isEmpty())
            builder.enchantments = Maps.newHashMap(enchantments);
        if(!flags.isEmpty())
            builder.flags = Sets.newHashSet(flags);
        if(!effects.isEmpty())
            builder.effects = Lists.newLinkedList(effects);
        builder.owner = owner;
        builder.perPlayerOwner = perPlayerOwner;
        builder.skinData = skinData;
        builder.unbreakable = unbreakable;
        return builder;
    }

    @Data
    @Getter
    public static class BabelData {

        @NonNull
        private final BabelMessageType type;
        private final Object[] params;

    }

}
