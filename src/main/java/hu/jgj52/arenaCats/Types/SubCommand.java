package hu.jgj52.arenaCats.Types;

import hu.jgj52.arenaCats.ArenaCats;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public abstract class SubCommand extends hu.jgj52.libCats.Types.SubCommand {
    @Override
    public JavaPlugin getPlugin() {
        return ArenaCats.plugin;
    }

    @Override
    public abstract boolean execute(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String @NotNull [] strings, @NotNull Player player);

    @Override
    public String getMsg(String msg) {
        return ArenaCats.messages.getConfig().getString("messages." + msg);
    }
}
