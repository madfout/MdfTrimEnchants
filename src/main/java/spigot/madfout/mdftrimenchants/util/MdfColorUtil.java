package spigot.madfout.mdftrimenchants.util;

import net.md_5.bungee.api.ChatColor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class MdfColorUtil {

    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");

    private MdfColorUtil() {}

    public static String translate(String message) {
        if (message == null) return "";

        Matcher matcher = HEX_PATTERN.matcher(message);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String hex = matcher.group(1);
            matcher.appendReplacement(sb,
                    Matcher.quoteReplacement(ChatColor.of("#" + hex).toString()));
        }
        matcher.appendTail(sb);
        message = sb.toString();

        message = org.bukkit.ChatColor.translateAlternateColorCodes('&', message);

        return message;
    }

    public static String stripColors(String message) {
        if (message == null) return "";
        String translated = translate(message);
        return org.bukkit.ChatColor.stripColor(translated);
    }

}