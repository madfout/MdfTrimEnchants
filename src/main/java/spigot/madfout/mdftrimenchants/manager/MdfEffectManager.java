package spigot.madfout.mdftrimenchants.manager;

import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import spigot.madfout.mdftrimenchants.MdfTrimEnchants;
import spigot.madfout.mdftrimenchants.model.MdfTrimEffect;
import spigot.madfout.mdftrimenchants.model.MdfTrimType;
import spigot.madfout.mdftrimenchants.util.MdfArmorScanner;

import java.util.*;

public class MdfEffectManager {

    private final MdfTrimEnchants plugin;
    private BukkitTask task;

    private static final String MODIFIER_PREFIX = "mdfte_";

    public MdfEffectManager(MdfTrimEnchants plugin) {
        this.plugin = plugin;
    }

    public void start() {
        int interval = plugin.getConfigManager().getAttributeUpdateInterval();
        task = plugin.getServer().getScheduler().runTaskTimer(plugin, this::updateAll, interval, interval);
    }

    public void stop() {
        if (task != null) {
            task.cancel();
            task = null;
        }
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            removeAllModifiers(player);
        }
    }

    private void updateAll() {
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            updatePlayer(player);
        }
    }

    public void updatePlayer(Player player) {
        removeAllModifiers(player);

        Map<MdfTrimType, Integer> activePieces = MdfArmorScanner.getActiveTrimPieces(player);
        if (activePieces.isEmpty()) return;

        Map<Attribute, Map<AttributeModifier.Operation, Double>> accumulated = new LinkedHashMap<>();

        for (Map.Entry<MdfTrimType, Integer> entry : activePieces.entrySet()) {
            double multiplier = MdfArmorScanner.getMultiplier(entry.getValue());
            for (MdfTrimEffect effect : entry.getKey().getEffects()) {
                accumulated
                        .computeIfAbsent(effect.getAttribute(), k -> new EnumMap<>(AttributeModifier.Operation.class))
                        .merge(effect.getOperation(), effect.getValue() * multiplier, Double::sum);
            }
        }

        for (Map.Entry<Attribute, Map<AttributeModifier.Operation, Double>> attrEntry : accumulated.entrySet()) {
            AttributeInstance instance = player.getAttribute(attrEntry.getKey());
            if (instance == null) continue;

            for (Map.Entry<AttributeModifier.Operation, Double> opEntry : attrEntry.getValue().entrySet()) {
                String name = MODIFIER_PREFIX + attrEntry.getKey().name() + "_" + opEntry.getKey().name();
                AttributeModifier modifier = new AttributeModifier(
                        UUID.nameUUIDFromBytes(name.getBytes()),
                        name,
                        opEntry.getValue(),
                        opEntry.getKey()
                );
                instance.addModifier(modifier);
            }
        }
    }

    public void removeAllModifiers(Player player) {
        for (Attribute attribute : Attribute.values()) {
            AttributeInstance instance = player.getAttribute(attribute);
            if (instance == null) continue;
            instance.getModifiers().stream()
                    .filter(m -> m.getName().startsWith(MODIFIER_PREFIX))
                    .toList()
                    .forEach(instance::removeModifier);
        }
    }

}