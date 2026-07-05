package spigot.madfout.mdftrimenchants.util;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import spigot.madfout.mdftrimenchants.MdfTrimEnchants;
import spigot.madfout.mdftrimenchants.model.MdfTrimType;

import java.util.LinkedHashMap;
import java.util.Map;

public final class MdfArmorScanner {

    private MdfArmorScanner() {}

    public static Map<MdfTrimType, Integer> getActiveTrimPieces(Player player) {
        MdfTrimEnchants plugin = MdfTrimEnchants.getInstance();
        boolean anySource = plugin.getConfigManager().isEffectsWorkOnAllTemplates();

        Map<String, Integer> keyCounts = new LinkedHashMap<>();
        ItemStack[] armor = player.getInventory().getArmorContents();
        for (ItemStack item : armor) {
            if (!anySource && !MdfTrimUtil.isIssued(item)) continue;

            MdfTrimUtil.getTrimKey(item).ifPresent(key ->
                    keyCounts.merge(key, 1, Integer::sum)
            );
        }

        Map<MdfTrimType, Integer> result = new LinkedHashMap<>();
        for (Map.Entry<String, Integer> entry : keyCounts.entrySet()) {
            MdfTrimType type = plugin.getConfigManager().getTrimType(entry.getKey());
            if (type != null && type.isEnabled()) {
                result.put(type, entry.getValue());
            }
        }
        return result;
    }

    public static double getMultiplier(int pieces) {
        int clamped = Math.min(4, Math.max(0, pieces));
        if (clamped == 0) return 0.0;

        boolean scaleByPieces = MdfTrimEnchants.getInstance().getConfigManager().isScaleEffectsByPieces();
        return scaleByPieces ? clamped / 4.0 : 1.0;
    }

}
