package spigot.madfout.mdftrimenchants.manager;

import org.bukkit.Particle;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import spigot.madfout.mdftrimenchants.MdfTrimEnchants;
import spigot.madfout.mdftrimenchants.model.MdfParticleConfig;
import spigot.madfout.mdftrimenchants.model.MdfTrimEffect;
import spigot.madfout.mdftrimenchants.model.MdfTrimType;
import spigot.madfout.mdftrimenchants.util.MdfColorUtil;

import java.io.File;
import java.util.*;
import java.util.logging.Logger;

public class MdfConfigManager {

    private final MdfTrimEnchants plugin;
    private final Logger log;

    private FileConfiguration messages;

    private final Map<String, MdfTrimType> trimTypes = new LinkedHashMap<>();

    private int attributeUpdateInterval;
    private int particleUpdateInterval;
    private boolean effectsWorkOnAllTemplates;
    private boolean scaleEffectsByPieces;

    public MdfConfigManager(MdfTrimEnchants plugin) {
        this.plugin = plugin;
        this.log = plugin.getLogger();
    }

    public void reload() {
        plugin.reloadConfig();
        FileConfiguration cfg = plugin.getConfig();

        String locale = cfg.getString("locale", "en");
        saveDefaultMessages();
        File msgFile = new File(plugin.getDataFolder(), "messages_" + locale + ".yml");
        if (!msgFile.exists()) {
            log.warning("File messages_" + locale + ".yml not found, is used messages_en.yml");
            msgFile = new File(plugin.getDataFolder(), "messages_en.yml");
        }
        messages = YamlConfiguration.loadConfiguration(msgFile);
        translateMessagesSection(messages);

        attributeUpdateInterval = Math.max(1, cfg.getInt("attribute-update-interval", 40));
        particleUpdateInterval  = Math.max(1, cfg.getInt("particle-update-interval", 10));
        effectsWorkOnAllTemplates = cfg.getBoolean("effects-work-on-all-templates", true);
        scaleEffectsByPieces = cfg.getBoolean("scale-effects-by-pieces", true);

        trimTypes.clear();
        loadTrimTypes(cfg);
        log.info("Loaded trim patterns: " + trimTypes.size());
    }

    private void loadTrimTypes(FileConfiguration cfg) {
        ConfigurationSection section = cfg.getConfigurationSection("trims");
        if (section == null) {
            log.warning("Section 'trims:' not found in config.yml!");
            return;
        }

        for (String key : section.getKeys(false)) {
            String upperKey = key.toUpperCase();
            ConfigurationSection sec = section.getConfigurationSection(key);
            if (sec == null) continue;

            try {
                MdfTrimType type = parseTrimType(upperKey, sec);
                trimTypes.put(upperKey, type);
            } catch (Exception e) {
                log.warning("Failed to load trim '" + key + "': " + e.getMessage());
            }
        }
    }

    private MdfTrimType parseTrimType(String key, ConfigurationSection sec) {
        boolean enabled     = sec.getBoolean("enabled", true);
        String displayName  = sec.getString("display-name", key);
        List<String> desc   = sec.getStringList("lore");

        List<MdfTrimEffect> effects = new ArrayList<>();
        for (Map<?, ?> map : sec.getMapList("effects")) {
            try {
                Attribute attr      = parseAttribute(String.valueOf(map.get("type")));
                double value        = Double.parseDouble(String.valueOf(map.get("value")));
                Object opRaw = map.get("operation");
                AttributeModifier.Operation op = parseOperation(
                        opRaw != null ? String.valueOf(opRaw) : "ADD_NUMBER");
                effects.add(new MdfTrimEffect(attr, value, op));
            } catch (Exception e) {
                log.warning("Invalid effect in trim '" + key + "': " + e.getMessage());
            }
        }

        MdfParticleConfig particles = parseParticles(key, sec.getConfigurationSection("particles"));

        return new MdfTrimType(key, enabled, displayName, desc, effects, particles);
    }

    private MdfParticleConfig parseParticles(String key, ConfigurationSection sec) {
        if (sec == null) return new MdfParticleConfig(false, Particle.ENCHANTMENT_TABLE, 5, 1.5, 0.05);

        boolean enabled    = sec.getBoolean("enabled", false);
        int count          = sec.getInt("count", 5);
        double radius      = sec.getDouble("radius", 1.5);
        double speed       = sec.getDouble("speed", 0.05);
        String particleName = sec.getString("particle", "ENCHANTMENT_TABLE").toUpperCase();

        Particle particle;
        try {
            particle = Particle.valueOf(particleName);
        } catch (IllegalArgumentException e) {
            log.warning("Unknown particle '" + particleName + "' for trim '" + key + "', using ENCHANTMENT_TABLE.");
            particle = Particle.ENCHANTMENT_TABLE;
        }

        return new MdfParticleConfig(enabled, particle, count, radius, speed);
    }

    private Attribute parseAttribute(String name) {
        return switch (name.toUpperCase()) {
            case "GENERIC_MOVEMENT_SPEED"          -> Attribute.GENERIC_MOVEMENT_SPEED;
            case "GENERIC_MAX_HEALTH"              -> Attribute.GENERIC_MAX_HEALTH;
            case "GENERIC_ATTACK_DAMAGE"           -> Attribute.GENERIC_ATTACK_DAMAGE;
            case "GENERIC_ATTACK_SPEED"            -> Attribute.GENERIC_ATTACK_SPEED;
            case "GENERIC_ARMOR"                   -> Attribute.GENERIC_ARMOR;
            case "GENERIC_ARMOR_TOUGHNESS"         -> Attribute.GENERIC_ARMOR_TOUGHNESS;
            case "GENERIC_KNOCKBACK_RESISTANCE"    -> Attribute.GENERIC_KNOCKBACK_RESISTANCE;
            case "GENERIC_LUCK"                    -> Attribute.GENERIC_LUCK;
            default -> throw new IllegalArgumentException("Unknown attribute: " + name);
        };
    }

    private AttributeModifier.Operation parseOperation(String name) {
        return switch (name.toUpperCase()) {
            case "ADD_NUMBER"        -> AttributeModifier.Operation.ADD_NUMBER;
            case "ADD_SCALAR"        -> AttributeModifier.Operation.ADD_SCALAR;
            case "MULTIPLY_SCALAR_1" -> AttributeModifier.Operation.MULTIPLY_SCALAR_1;
            default -> throw new IllegalArgumentException("Unknown operation: " + name);
        };
    }

    private void saveDefaultMessages() {
        for (String lang : new String[]{"ru", "en"}) {
            File f = new File(plugin.getDataFolder(), "messages_" + lang + ".yml");
            if (!f.exists()) {
                plugin.saveResource("messages_" + lang + ".yml", false);
            }
        }
    }

    private void translateMessagesSection(FileConfiguration config) {
        for (String key : config.getKeys(true)) {
            if (config.isString(key)) {
                config.set(key, MdfColorUtil.translate(config.getString(key)));
            } else if (config.isList(key)) {
                List<?> rawList = config.getList(key);
                if (rawList == null) continue;

                List<String> translated = new ArrayList<>();
                for (Object entry : rawList) {
                    translated.add(entry instanceof String s
                            ? MdfColorUtil.translate(s)
                            : String.valueOf(entry));
                }
                config.set(key, translated);
            }
        }
    }


    public Map<String, MdfTrimType> getTrimTypes()        { return Collections.unmodifiableMap(trimTypes); }
    public MdfTrimType getTrimType(String key)            { return trimTypes.get(key.toUpperCase()); }
    public int getTrimCount()                             { return trimTypes.size(); }
    public int getAttributeUpdateInterval()               { return attributeUpdateInterval; }
    public int getParticleUpdateInterval()                { return particleUpdateInterval; }
    public boolean isEffectsWorkOnAllTemplates()           { return effectsWorkOnAllTemplates; }
    public boolean isScaleEffectsByPieces()                { return scaleEffectsByPieces; }
    public FileConfiguration getMessages()                { return messages; }

    public String getMessage(String path) {
        String msg = messages.getString("messages." + path);
        return msg != null ? msg : "[MdfTrimEnchants] " + path + " not found";
    }

    public List<String> getMessageList(String path) {
        List<String> list = messages.getStringList("messages." + path);
        return list.isEmpty()
                ? Collections.singletonList("[MdfTrimEnchants] " + path + " not found")
                : list;
    }

}
