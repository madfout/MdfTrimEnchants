package spigot.madfout.mdftrimenchants.command;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import spigot.madfout.mdftrimenchants.MdfTrimEnchants;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class MdfTabCompleter implements TabCompleter {

    private static final List<String> SUB_COMMANDS = Arrays.asList("reload", "help", "status", "list", "give");
    private static final List<String> AMOUNT_SUGGESTIONS = Arrays.asList("1", "8", "16", "24", "32", "48", "64");

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            StringUtil.copyPartialMatches(args[0], SUB_COMMANDS, completions);
        } else if (args.length == 2 && args[0].equalsIgnoreCase("status")
                && sender.hasPermission("mte.status.others")) {
            List<String> names = Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .collect(Collectors.toList());
            StringUtil.copyPartialMatches(args[1], names, completions);
        } else if (args[0].equalsIgnoreCase("give") && sender.hasPermission("mte.give")) {
            if (args.length == 2) {
                List<String> names = Bukkit.getOnlinePlayers().stream()
                        .map(Player::getName)
                        .collect(Collectors.toList());
                StringUtil.copyPartialMatches(args[1], names, completions);
            } else if (args.length == 3) {
                List<String> trims = new ArrayList<>(
                        MdfTrimEnchants.getInstance().getConfigManager().getTrimTypes().keySet());
                StringUtil.copyPartialMatches(args[2], trims, completions);
            } else if (args.length == 4) {
                StringUtil.copyPartialMatches(args[3], AMOUNT_SUGGESTIONS, completions);
            }
        }

        return completions;
    }

}