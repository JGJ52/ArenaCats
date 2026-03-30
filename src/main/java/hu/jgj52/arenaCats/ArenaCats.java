package hu.jgj52.arenaCats;

import hu.jgj52.arenaCats.Commands.ArenaCommand;
import hu.jgj52.arenaCats.Configurations.ArenasConfiguration;
import hu.jgj52.arenaCats.Configurations.MessagesConfiguration;
import hu.jgj52.arenaCats.Listeners.ArenaEditorListener;
import hu.jgj52.arenaCats.Types.ArenaEditor;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;

public final class ArenaCats extends JavaPlugin {
    public static ArenaCats plugin;
    public static MessagesConfiguration messages;
    public static ArenasConfiguration arenas;

    @Override
    public void onEnable() {
        // Plugin startup logic
        plugin = this;
        messages = new MessagesConfiguration();
        arenas = new ArenasConfiguration();

        new ArenaCommand().register();

        getServer().getPluginManager().registerEvents(new ArenaEditorListener(), this);

        for (World world : Bukkit.getWorlds()) {
            if (world.getName().endsWith("_map_editor_arenacats") || world.getName().endsWith("_map_placed_arenacats")) {
                ArenaEditor.deleteWorld(world);
            }
        }
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
