package spigot.madfout.mdftrimenchants.util;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ArmorMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.trim.ArmorTrim;
import org.bukkit.persistence.PersistentDataType;
import spigot.madfout.mdftrimenchants.MdfTrimEnchants;
import spigot.madfout.mdftrimenchants.model.MdfTrimType;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class MdfTrimUtil {

    private MdfTrimUtil() {}

    private static final NamespacedKey ISSUED_KEY =
            new NamespacedKey(MdfTrimEnchants.getInstance(), "issued_template");

    public static Optional<String> getTrimKey(ItemStack item) {
        if (item == null || item.getType().isAir()) return Optional.empty();
        if (!(item.getItemMeta() instanceof ArmorMeta meta)) return Optional.empty();
        ArmorTrim trim = meta.getTrim();
        if (trim == null) return Optional.empty();
        return Optional.of(trim.getPattern().getKey().getKey().toUpperCase());
    }

    public static boolean isArmorItem(ItemStack item) {
        if (item == null || item.getType().isAir()) return false;
        String name = item.getType().name();
        return name.endsWith("_HELMET") || name.endsWith("_CHESTPLATE")
                || name.endsWith("_LEGGINGS") || name.endsWith("_BOOTS");
    }

    public static ItemStack createTemplateItem(MdfTrimType type, int amount) {
        Material material;
        try {
            material = Material.valueOf(type.getKey() + "_ARMOR_TRIM_SMITHING_TEMPLATE");
        } catch (IllegalArgumentException e) {
            return null;
        }

        ItemStack item = new ItemStack(material, amount);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(MdfColorUtil.translate(type.getDisplayName()));

            List<String> lore = new ArrayList<>();
            for (String line : type.getLore()) {
                lore.add(MdfColorUtil.translate(line));
            }
            meta.setLore(lore);

            meta.getPersistentDataContainer().set(ISSUED_KEY, PersistentDataType.BYTE, (byte) 1);

            item.setItemMeta(meta);
        }
        return item;
    }

    public static void markAsIssued(ItemStack item) {
        if (item == null) return;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;
        meta.getPersistentDataContainer().set(ISSUED_KEY, PersistentDataType.BYTE, (byte) 1);
        item.setItemMeta(meta);
    }

    public static boolean isIssued(ItemStack item) {
        if (item == null || item.getType().isAir()) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;
        return meta.getPersistentDataContainer().has(ISSUED_KEY, PersistentDataType.BYTE);
    }

}