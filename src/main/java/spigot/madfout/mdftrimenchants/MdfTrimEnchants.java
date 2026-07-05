package spigot.madfout.mdftrimenchants;

import org.bukkit.plugin.java.JavaPlugin;

import spigot.madfout.mdftrimenchants.command.MdfCommand;
import spigot.madfout.mdftrimenchants.command.MdfTabCompleter;
import spigot.madfout.mdftrimenchants.listener.MdfListener;
import spigot.madfout.mdftrimenchants.manager.MdfConfigManager;
import spigot.madfout.mdftrimenchants.manager.MdfEffectManager;
import spigot.madfout.mdftrimenchants.manager.MdfParticleManager;

public final class MdfTrimEnchants extends JavaPlugin {

    private static MdfTrimEnchants instance;

    private MdfConfigManager configManager;
    private MdfEffectManager effectManager;
    private MdfParticleManager particleManager;

    @Override
    public void onEnable() {
        instance = this;

        getServer().getConsoleSender().sendMessage(" \n§a❱ §fThe plugin §a" + getDescription().getName() + " §fenabled!\n§a❱ §fPlugin version: §a" + getDescription().getVersion() + " \n§a❱ §fBy §amadfout\n ");

        saveDefaultConfig();

        configManager = new MdfConfigManager(this);
        configManager.reload();

        effectManager = new MdfEffectManager(this);
        effectManager.start();

        particleManager = new MdfParticleManager(this);
        particleManager.start();

        getServer().getPluginManager().registerEvents(new MdfListener(this), this);

        MdfCommand mdfCommand = new MdfCommand(this);
        if (getCommand("mte") != null) {
            getCommand("mte").setExecutor(mdfCommand);
            getCommand("mte").setTabCompleter(new MdfTabCompleter());
        }
        // Plugin startup logic
    }

    @Override
    public void onDisable() {
        if (effectManager != null) effectManager.stop();
        if (particleManager != null) particleManager.stop();

        getServer().getConsoleSender().sendMessage(" \n§c❱ §fThe plugin §c" + getDescription().getName() + " §fdisabled!\n§c❱ §fPlugin version: §c" + getDescription().getVersion() + " \n§c❱ §fBy §cmadfout\n ");
        // Plugin shutdown logic
    }

    public void reload() {
        configManager.reload();
        effectManager.stop();
        effectManager.start();
        particleManager.stop();
        particleManager.start();
    }

    public static MdfTrimEnchants getInstance()        { return instance; }
    public MdfConfigManager getConfigManager()          { return configManager; }
    public MdfEffectManager getEffectManager()          { return effectManager; }
    public MdfParticleManager getParticleManager()      { return particleManager; }

}
