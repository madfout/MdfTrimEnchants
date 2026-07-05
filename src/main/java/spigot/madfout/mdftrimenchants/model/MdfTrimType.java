package spigot.madfout.mdftrimenchants.model;

import java.util.List;

public class MdfTrimType {

    private final String key;
    private final boolean enabled;
    private final String displayName;
    private final List<String> lore;
    private final List<MdfTrimEffect> effects;
    private final MdfParticleConfig particleConfig;

    public MdfTrimType(String key, boolean enabled, String displayName,
                       List<String> lore, List<MdfTrimEffect> effects,
                       MdfParticleConfig particleConfig) {
        this.key = key;
        this.enabled = enabled;
        this.displayName = displayName;
        this.lore = lore;
        this.effects = effects;
        this.particleConfig = particleConfig;
    }

    public String getKey()                        { return key; }
    public boolean isEnabled()                    { return enabled; }
    public String getDisplayName()                { return displayName; }
    public List<String> getLore()                 { return lore; }
    public List<MdfTrimEffect> getEffects()       { return effects; }
    public MdfParticleConfig getParticleConfig()  { return particleConfig; }

}