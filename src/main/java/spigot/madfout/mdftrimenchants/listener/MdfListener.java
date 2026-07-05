package spigot.madfout.mdftrimenchants.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.PrepareSmithingEvent;
import org.bukkit.event.player.PlayerItemBreakEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.SmithingInventory;
import spigot.madfout.mdftrimenchants.MdfTrimEnchants;
import spigot.madfout.mdftrimenchants.util.MdfTrimUtil;

public class MdfListener implements Listener {

    private final MdfTrimEnchants plugin;

    public MdfListener(MdfTrimEnchants plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        boolean isArmorSlot = event.getSlotType() == InventoryType.SlotType.ARMOR;
        boolean isPlayerArmorSlot = event.getInventory().getType() == InventoryType.PLAYER
                && event.getSlot() >= 36 && event.getSlot() <= 39;

        if (isArmorSlot || isPlayerArmorSlot) {
            scheduleUpdate(player);
            return;
        }

        if (event.isShiftClick()) {
            ItemStack clicked = event.getCurrentItem();
            if (clicked != null && MdfTrimUtil.isArmorItem(clicked)) {
                scheduleUpdate(player);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onItemBreak(PlayerItemBreakEvent event) {
        if (MdfTrimUtil.isArmorItem(event.getBrokenItem())) {
            scheduleUpdate(event.getPlayer());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerDeath(PlayerDeathEvent event) {
        scheduleUpdate(event.getEntity());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        scheduleUpdate(event.getPlayer());
    }

    @EventHandler
    public void onPrepareSmithing(PrepareSmithingEvent event) {
        SmithingInventory inv = event.getInventory();
        ItemStack template = inv.getInputTemplate();
        ItemStack result = event.getResult();

        if (template != null && result != null
                && MdfTrimUtil.isArmorItem(result)
                && MdfTrimUtil.isIssued(template)) {
            MdfTrimUtil.markAsIssued(result);
            event.setResult(result);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        plugin.getEffectManager().removeAllModifiers(event.getPlayer());
    }

    private void scheduleUpdate(Player player) {
        plugin.getServer().getScheduler().runTaskLater(plugin,
                () -> plugin.getEffectManager().updatePlayer(player), 1L);
    }

}
