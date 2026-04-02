package hu.jgj52.arenaCats.SubCommands.Arena;

import hu.jgj52.arenaCats.Types.Arena;
import hu.jgj52.arenaCats.Types.PlacedArena;
import hu.jgj52.arenaCats.Types.SubCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static hu.jgj52.arenaCats.ArenaCats.arenas;

public class Place extends SubCommand {
    @Override
    public String getName() {
        return "place";
    }

    @Override
    public boolean execute(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String @NotNull [] strings, @NotNull Player player) {
        if (strings.length < 2) {
            player.sendMessage(getComp("noArgs"));
            return true;
        }
        Arena arena = Arena.of(strings[1]);
        if (arena == null) {
            player.sendMessage(getComp("arenaNotFound"));
            return true;
        }
        PlacedArena placed = arena.place();
        placed.onInit(() -> placed.teleportToCenter(player));
        return true;
    }

    @Override
    public List<String> complete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String @NotNull [] strings) {
        if (strings.length == 2) {
            return new ArrayList<>(arenas.getConfig().getKeys(false));
        }
        return List.of();
    }

    @Override
    public boolean firstComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String @NotNull [] strings) {
        return true;
    }
}
