package hu.jgj52.arenaCats.Types;

import hu.jgj52.arenaCats.ArenaCats;
import org.bukkit.plugin.java.JavaPlugin;

public abstract class Configuration extends hu.jgj52.libCats.Types.Configuration {
    @Override
    public JavaPlugin getPlugin() {
        return ArenaCats.plugin;
    }
}
