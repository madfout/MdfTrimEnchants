package spigot.madfout.mdftrimenchants.command;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import spigot.madfout.mdftrimenchants.MdfTrimEnchants;
import spigot.madfout.mdftrimenchants.manager.MdfConfigManager;
import spigot.madfout.mdftrimenchants.model.MdfTrimEffect;
import spigot.madfout.mdftrimenchants.model.MdfTrimType;
import spigot.madfout.mdftrimenchants.util.MdfArmorScanner;
import spigot.madfout.mdftrimenchants.util.MdfColorUtil;
import spigot.madfout.mdftrimenchants.util.MdfTrimUtil;

import java.util.List;
import java.util.Map;

public class MdfCommand implements CommandExecutor {

    private final MdfTrimEnchants plugin;

    public MdfCommand(MdfTrimEnchants plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        MdfConfigManager cfg = plugin.getConfigManager();

        if (args.length == 0) {
            sender.sendMessage(cfg.getMessage("unknown-command"));
            return true;
        }

        switch (args[0].toLowerCase()) {

            case "reload": {
                if (!sender.hasPermission("mte.reload")) {
                    sender.sendMessage(cfg.getMessage("no-permission"));
                    return true;
                }
                try {
                    plugin.reload();
                    sender.sendMessage(plugin.getConfigManager().getMessage("reload-success"));
                } catch (Exception e) {
                    sender.sendMessage(MdfColorUtil.translate(
                            cfg.getMessage("reload-fail").replace("{error}", e.getMessage())));
                }
                return true;
            }

            case "help": {
                if (!sender.hasPermission("mte.help")) {
                    sender.sendMessage(cfg.getMessage("no-permission"));
                    return true;
                }
                for (String line : cfg.getMessageList("help")) {
                    sender.sendMessage(line);
                }
                return true;
            }

            case "status": {
                if (!sender.hasPermission("mte.status")) {
                    sender.sendMessage(cfg.getMessage("no-permission"));
                    return true;
                }

                Player target;
                if (args.length >= 2) {
                    if (!sender.hasPermission("mte.status.others")) {
                        sender.sendMessage(cfg.getMessage("no-permission"));
                        return true;
                    }
                    target = Bukkit.getPlayerExact(args[1]);
                    if (target == null) {
                        sender.sendMessage(MdfColorUtil.translate(
                                cfg.getMessage("player-not-found").replace("{player}", args[1])));
                        return true;
                    }
                } else {
                    if (!(sender instanceof Player)) {
                        sender.sendMessage(cfg.getMessage("player-only"));
                        return true;
                    }
                    target = (Player) sender;
                }

                for (String line : cfg.getMessageList("status-header")) {
                    sender.sendMessage(MdfColorUtil.translate(line.replace("{player}", target.getName())));
                }

                Map<MdfTrimType, Integer> activePieces = MdfArmorScanner.getActiveTrimPieces(target);
                if (activePieces.isEmpty()) {
                    sender.sendMessage(cfg.getMessage("status-no-armor"));
                    return true;
                }

                for (Map.Entry<MdfTrimType, Integer> entry : activePieces.entrySet()) {
                    MdfTrimType type = entry.getKey();
                    int pieces = entry.getValue();
                    int percent = (int) (MdfArmorScanner.getMultiplier(pieces) * 100);

                    sender.sendMessage(MdfColorUtil.translate(cfg.getMessage("status-trim-line")
                            .replace("{trim}", type.getKey())
                            .replace("{name}", MdfColorUtil.translate(type.getDisplayName()))
                            .replace("{percent}", String.valueOf(percent))
                            .replace("{pieces}", String.valueOf(pieces))));

                    for (MdfTrimEffect effect : type.getEffects()) {
                        double scaledValue = effect.getValue() * MdfArmorScanner.getMultiplier(pieces);
                        sender.sendMessage(MdfColorUtil.translate(cfg.getMessage("status-effect-line")
                                .replace("{attribute}", effect.getAttribute().name())
                                .replace("{operation}", effect.getOperation().name())
                                .replace("{value}", String.format("%.4f", scaledValue))));
                    }
                }

                sender.sendMessage(cfg.getMessage("status-footer"));
                return true;
            }

            case "list": {
                if (!sender.hasPermission("mte.list")) {
                    sender.sendMessage(cfg.getMessage("no-permission"));
                    return true;
                }
                for (String line : cfg.getMessageList("list-header")) {
                    sender.sendMessage(line);
                }
                for (MdfTrimType type : cfg.getTrimTypes().values()) {
                    String msgKey = type.isEnabled() ? "list-trim-enabled" : "list-trim-disabled";
                    sender.sendMessage(MdfColorUtil.translate(cfg.getMessage(msgKey)
                            .replace("{trim}", type.getKey())
                            .replace("{name}", MdfColorUtil.translate(type.getDisplayName()))));
                }
                sender.sendMessage(cfg.getMessage("list-footer"));
                return true;
            }

            case "give": {
                if (!sender.hasPermission("mte.give")) {
                    sender.sendMessage(cfg.getMessage("no-permission"));
                    return true;
                }

                if (args.length < 3) {
                    sender.sendMessage(MdfColorUtil.translate(cfg.getMessage("give-usage")));
                    return true;
                }

                Player target = Bukkit.getPlayerExact(args[1]);
                if (target == null) {
                    sender.sendMessage(MdfColorUtil.translate(
                            cfg.getMessage("player-not-found").replace("{player}", args[1])));
                    return true;
                }

                MdfTrimType type = cfg.getTrimType(args[2]);
                if (type == null) {
                    sender.sendMessage(MdfColorUtil.translate(
                            cfg.getMessage("trim-not-found").replace("{trim}", args[2])));
                    return true;
                }

                int amount = 1;
                if (args.length >= 4) {
                    try {
                        amount = Integer.parseInt(args[3]);
                    } catch (NumberFormatException e) {
                        sender.sendMessage(MdfColorUtil.translate(
                                cfg.getMessage("invalid-amount").replace("{amount}", args[3])));
                        return true;
                    }
                }
                amount = Math.max(1, Math.min(64, amount));

                ItemStack template = MdfTrimUtil.createTemplateItem(type, amount);
                if (template == null) {
                    sender.sendMessage(MdfColorUtil.translate(
                            cfg.getMessage("trim-not-found").replace("{trim}", args[2])));
                    return true;
                }

                Map<Integer, ItemStack> leftover = target.getInventory().addItem(template);
                for (ItemStack drop : leftover.values()) {
                    target.getWorld().dropItemNaturally(target.getLocation(), drop);
                }

                String translatedName = MdfColorUtil.translate(type.getDisplayName());

                sender.sendMessage(MdfColorUtil.translate(cfg.getMessage("give-success")
                        .replace("{player}", target.getName())
                        .replace("{amount}", String.valueOf(amount))
                        .replace("{name}", translatedName)));

                if (!target.equals(sender)) {
                    target.sendMessage(MdfColorUtil.translate(cfg.getMessage("give-received")
                            .replace("{amount}", String.valueOf(amount))
                            .replace("{name}", translatedName)));
                }
                return true;
            }

            default: {
                sender.sendMessage(cfg.getMessage("unknown-command"));
                return true;
            }
        }
    }

}
