package hu.jgj52.arenaCats.SubCommands.Arena;

import hu.jgj52.arenaCats.Types.ArenaEditor;
import hu.jgj52.arenaCats.Types.PlacedArena;
import hu.jgj52.arenaCats.Types.SubCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Waypoint extends SubCommand {
    @Override
    public String getName() {
        return "waypoint";
    }

    @Override
    public boolean execute(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String @NotNull [] strings, @NotNull Player player) {
        ArenaEditor editor = ArenaEditor.getEditor(player);
        if (editor != null) {
            if (!player.getWorld().getName().equals(editor.getWorld().getName())) return true;
            if (strings.length < 2) {
                player.sendMessage(getComp("noArgs"));
                return true;
            }
            for (hu.jgj52.arenaCats.Types.Waypoint waypoint : editor.getWaypoints()) {
                if (waypoint.name().equals(strings[1])) {
                    player.sendMessage(getComp("alreadyWaypoint"));
                    return true;
                }
            }
            editor.waypoint(strings[1]);
            player.sendMessage(getComp("madeWaypoint"));
        } else if (player.getWorld().getName().endsWith("_map_placed_arenacats")) {
            PlacedArena arena = PlacedArena.getPlacedArena(UUID.fromString(player.getWorld().getName().replace("_map_placed_arenacats", "")));
            hu.jgj52.arenaCats.Types.Waypoint waypoint = arena.getWaypoint(strings[1]);
            if (waypoint == null) {
                player.sendMessage(getComp("waypointNotFound"));
                return true;
            }
            arena.teleportToWaypoint(player, waypoint);
        }
        return true;
    }

    @Override
    public List<String> complete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String @NotNull [] strings) {
        if (!firstComplete(commandSender, command, s, strings)) return List.of();
        if (!(commandSender instanceof Player player)) return List.of();
        if (strings.length == 2) {
            if (ArenaEditor.getEditor(player) != null && player.getWorld().getName().equals(ArenaEditor.getEditor(player).getWorld().getName())) {
                return List.of("<name>");
            } else if (player.getWorld().getName().endsWith("_map_placed_arenacats")) {
                PlacedArena arena = PlacedArena.getPlacedArena(UUID.fromString(player.getWorld().getName().replace("_map_placed_arenacats", "")));
                List<String> complete = new ArrayList<>();
                for (hu.jgj52.arenaCats.Types.Waypoint waypoint : arena.getWaypoints()) {
                    complete.add(waypoint.name());
                }
                return complete;
            }
        }
        return List.of();
    }

    @Override
    public boolean firstComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String @NotNull [] strings) {
        if (!(commandSender instanceof Player player)) return false;
        return (ArenaEditor.getEditor(player) != null && player.getWorld().getName().equals(ArenaEditor.getEditor(player).getWorld().getName())) ||
                (player.getWorld().getName().endsWith("_map_placed_arenacats") && !PlacedArena.getPlacedArena(UUID.fromString(player.getWorld().getName().replace("_map_placed_arenacats", ""))).getWaypoints().isEmpty()) ;
    }
}
