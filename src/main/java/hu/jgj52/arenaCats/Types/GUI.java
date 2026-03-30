package hu.jgj52.arenaCats.Types;

import hu.jgj52.arenaCats.ArenaCats;
import org.bukkit.plugin.java.JavaPlugin;

public abstract class GUI extends hu.jgj52.libCats.Types.GUI {
    @Override
    public JavaPlugin getPlugin() {
        return ArenaCats.plugin;
    }

    @Override
    public String getMessage(String msg) {
        return ArenaCats.messages.getConfig().getString("guis." + getClass().getSimpleName() + "." + msg);
    }

    @Override
    public String getMsg(String msg) {
        return ArenaCats.messages.getConfig().getString("messages." + msg);
    }
}
