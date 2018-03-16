package net.minespree.wizard;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import lombok.Getter;
import net.minespree.wizard.floatingtext.FloatingTextListener;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class WizardPlugin extends JavaPlugin {
    @Getter
    private static WizardPlugin plugin;
    @Getter
    private ProtocolManager protocolManager;

    @Override
    public void onEnable() {
        plugin = this;

        protocolManager = ProtocolLibrary.getProtocolManager();

        Bukkit.getPluginManager().registerEvents(new FloatingTextListener(), this);
    }
}
