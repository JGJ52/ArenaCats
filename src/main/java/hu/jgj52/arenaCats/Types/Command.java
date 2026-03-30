package hu.jgj52.arenaCats.Types;

import hu.jgj52.arenaCats.ArenaCats;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public abstract class Command extends hu.jgj52.libCats.Types.Command {
    @Override
    public JavaPlugin getPlugin() {
        return ArenaCats.plugin;
    }

    @Override
    public Consumer<CommandSender> noPermission() {
        return sender -> sender.sendMessage(getMsg("noPerm"));
    }

    @Override
    public abstract boolean execute(@NotNull CommandSender commandSender, @NotNull org.bukkit.command.Command command, @NotNull String s, @NotNull String @NotNull [] strings, @NotNull Player player);

    @Override
    public Consumer<CommandSender> notPlayer() {
        return sender -> sender.sendMessage(getMsg("notPlayer"));
    }

    @Override
    public String getMsg(String msg) {
        return ArenaCats.messages.getConfig().getString("messages." + msg);
    }
}
