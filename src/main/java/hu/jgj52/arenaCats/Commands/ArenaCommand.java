package hu.jgj52.arenaCats.Commands;

import hu.jgj52.arenaCats.GUIs.ArenaGUI;
import hu.jgj52.arenaCats.SubCommands.Arena.*;
import hu.jgj52.arenaCats.Types.Command;
import hu.jgj52.libCats.Types.SubCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ArenaCommand extends Command {
    @Override
    public String getName() {
        return "arena";
    }

    @Override
    public List<SubCommand> getSubCommands() {
        return List.of(
                new Create(),
                new Waypoint(),
                new Place(),
                new Delete()
        );
    }

    @Override
    public boolean execute(@NotNull CommandSender commandSender, org.bukkit.command.@NotNull Command command, @NotNull String s, @NotNull String @NotNull [] strings, @NotNull Player player) {
        new ArenaGUI().open(player);
        return true;
    }
}