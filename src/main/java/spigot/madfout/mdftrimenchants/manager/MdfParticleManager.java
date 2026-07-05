package spigot.madfout.mdftrimenchants.manager;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import spigot.madfout.mdftrimenchants.MdfTrimEnchants;
import spigot.madfout.mdftrimenchants.model.MdfParticleConfig;
import spigot.madfout.mdftrimenchants.model.MdfTrimType;
import spigot.madfout.mdftrimenchants.util.MdfArmorScanner;

import java.util.Map;

public class MdfParticleManager {

    private final MdfTrimEnchants plugin;
    private BukkitTask task;

    public MdfParticleManager(MdfTrimEnchants plugin) {
        this.plugin = plugin;
    }

    public void start() {
        int interval = plugin.getConfigManager().getParticleUpdateInterval();
        task = plugin.getServer().getScheduler().runTaskTimer(plugin, this::spawnAll, interval, interval);
    }

    public void stop() {
        if (task != null) {
            task.cancel();
            task = null;
        }
    }

    private void spawnAll() {
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            spawnForPlayer(player);
        }
    }

    private void spawnForPlayer(Player player) {
        Map<MdfTrimType, Integer> activePieces = MdfArmorScanner.getActiveTrimPieces(player);
        for (Map.Entry<MdfTrimType, Integer> entry : activePieces.entrySet()) {
            if (entry.getValue() < 4) continue;

            MdfParticleConfig pc = entry.getKey().getParticleConfig();
            if (!pc.isEnabled()) continue;

            spawnRing(player, pc);
        }
    }

    private void spawnRing(Player player, MdfParticleConfig pc) {
        Location center = player.getLocation().add(0, 1.0, 0);
        double radius   = pc.getRadius();
        int count       = pc.getCount();
        double speed    = pc.getSpeed();
        double step     = (2 * Math.PI) / count;

        for (int i = 0; i < count; i++) {
            double angle = i * step;
            double x = Math.cos(angle) * radius;
            double z = Math.sin(angle) * radius;
            try {
                player.getWorld().spawnParticle(pc.getParticle(), center.clone().add(x, 0, z), 1, 0, 0, 0, speed);
            } catch (Exception ignored) {
            }
        }
    }

}