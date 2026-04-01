package hu.jgj52.arenaCats.Types;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.managers.storage.StorageException;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import hu.jgj52.libCats.Utils.RegistryFromName;
import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.stream.Stream;

import static hu.jgj52.arenaCats.ArenaCats.plugin;

public class PlacedArena {
    private static final Map<UUID, PlacedArena> placedArenas = new HashMap<>();
    public static PlacedArena getPlacedArena(UUID uuid) {
        return placedArenas.get(uuid);
    }
    public static Set<UUID> all() {
        return placedArenas.keySet();
    }
    private World world;
    private final List<ProtectedRegion> regions = new ArrayList<>();
    private final List<Waypoint> waypoints = new ArrayList<>();
    private final List<Runnable> runnables = new ArrayList<>();
    private boolean inited = false;
    PlacedArena(Clipboard clipboard, ConfigurationSection regions, ConfigurationSection waypoints, ConfigurationSection gamerules, Difficulty difficulty) {
        UUID uuid = UUID.randomUUID();
        placedArenas.put(uuid, this);

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            copyFolder(
                    Bukkit.getWorldContainer().toPath().resolve("empty_arenacats"),
                    Bukkit.getWorldContainer().toPath().resolve(uuid + "_map_placed_arenacats")
            );
            File file = new File(Bukkit.getWorldContainer().toPath().resolve(uuid + "_map_placed_arenacats" + File.separator + "uid.dat").toUri());
            if (file.exists()) file.delete();

            Bukkit.getScheduler().runTask(plugin, () -> {
                WorldCreator creator = new WorldCreator(uuid + "_map_placed_arenacats");
                creator.generatorSettings("{\"layers\":[{\"block\":\"air\",\"height\":1}],\"biome\":\"plains\"}");
                creator.generateStructures(false);
                creator.type(WorldType.FLAT);
                world = creator.createWorld();
                if (world == null) return;
                world.setDifficulty(difficulty);
                if (gamerules != null) {
                    String ver = Bukkit.getMinecraftVersion();
                    if (ver.equals("1.21.11") || !ver.startsWith("1.")) {
                        for (String gamerule : gamerules.getKeys(false)) {
                            GameRule<?> rule = RegistryFromName.GAME_RULE(gamerule);
                            Object value = gamerules.get(gamerule);
                            if (value != null) {
                                if (rule.getType() == Boolean.class) {
                                    world.setGameRule((GameRule<Boolean>) rule, Boolean.parseBoolean(value.toString()));
                                } else if (rule.getType() == Integer.class) {
                                    world.setGameRule((GameRule<Integer>) rule, Integer.parseInt(value.toString()));
                                } else if (rule.getType() == Double.class) {
                                    world.setGameRule((GameRule<Double>) rule, Double.parseDouble(value.toString()));
                                }
                            }
                        }
                    } else {
                        for (String gamerule : gamerules.getKeys(false)) {
                            @SuppressWarnings("removal")
                            GameRule<?> rule = GameRule.getByName(gamerule);
                            Object value = gamerules.get(gamerule);
                            if (rule != null && value != null) {
                                if (rule.getType() == Boolean.class) {
                                    world.setGameRule((GameRule<Boolean>) rule, Boolean.parseBoolean(value.toString()));
                                } else if (rule.getType() == Integer.class) {
                                    world.setGameRule((GameRule<Integer>) rule, Integer.parseInt(value.toString()));
                                } else if (rule.getType() == Double.class) {
                                    world.setGameRule((GameRule<Double>) rule, Double.parseDouble(value.toString()));
                                }
                            }
                        }
                    }
                }
                world.setTime(6000);
                world.setWeatherDuration(0);
                com.sk89q.worldedit.world.World weWorld = BukkitAdapter.adapt(world);
                try (EditSession session = WorldEdit.getInstance().newEditSession(weWorld)) {
                    Operation operation = new ClipboardHolder(clipboard)
                            .createPaste(session)
                            .to(BlockVector3.at(0, 0, 0))
                            .ignoreAirBlocks(true)
                            .build();
                    Operations.complete(operation);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                Plugin we = Bukkit.getPluginManager().getPlugin("WorldGuard");
                if (we != null && we.isEnabled() && regions != null) {
                    RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
                    RegionManager rgs = container.get(weWorld);
                    if (rgs != null) {
                        for (String name : regions.getKeys(false)) {
                            double x1 = regions.getDouble(name + ".x1");
                            double y1 = regions.getDouble(name + ".y1");
                            double z1 = regions.getDouble(name + ".z1");
                            double x2 = regions.getDouble(name + ".x2");
                            double y2 = regions.getDouble(name + ".y2");
                            double z2 = regions.getDouble(name + ".z2");
                            ConfigurationSection flagsSection = regions.getConfigurationSection(name + ".flags");
                            Map<StateFlag, StateFlag.State> flags = new HashMap<>();
                            if (flagsSection != null) {
                                for (String n : flagsSection.getKeys(false)) {
                                    if (!(WorldGuard.getInstance().getFlagRegistry().get(n.toLowerCase()) instanceof StateFlag flag))
                                        continue;
                                    StateFlag.State state = StateFlag.State.valueOf(flagsSection.getString(n));
                                    flags.put(flag, state);
                                }
                            }
                            BlockVector3 min = BlockVector3.at(Math.min(x1, x2), Math.min(y1, y2), Math.min(z1, z2));
                            BlockVector3 max = BlockVector3.at(Math.max(x1, x2), Math.max(y1, y2), Math.max(z1, z2));
                            ProtectedRegion region = new ProtectedCuboidRegion(name, min, max);
                            if (regions.get(name + ".priority") != null) {
                                region.setPriority(regions.getInt(name + ".priority"));
                            }
                            flags.forEach(region::setFlag);
                            rgs.addRegion(region);
                            this.regions.add(region);
                        }
                        try {
                            rgs.save();
                        } catch (StorageException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
                if (waypoints != null) {
                    for (String name : waypoints.getKeys(false)) {
                        Location loc = new Location(
                                world,
                                waypoints.getDouble(name + ".x"),
                                waypoints.getDouble(name + ".y"),
                                waypoints.getDouble(name + ".z"),
                                (float) waypoints.getDouble(name + ".yaw"),
                                (float) waypoints.getDouble(name + ".pitch")
                        );
                        Waypoint waypoint = new Waypoint(name, loc);
                        this.waypoints.add(waypoint);
                    }
                }
                inited = true;
                for (Runnable runnable : runnables) {
                    runnable.run();
                }
            });
        });
        // such long constructor
    }

    public void onInit(Runnable runnable) {
        if (inited) {
            runnable.run();
        } else {
            runnables.add(runnable);
        }
    }

    private static void copyFolder(Path src, Path dest) {
        try {
            try (Stream<Path> stream = Files.walk(src)) {
                stream.forEach(path -> {
                    try {
                        Files.copy(path, dest.resolve(src.relativize(path)),
                                StandardCopyOption.REPLACE_EXISTING);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public World getWorld() {
        return world;
    }

    public List<ProtectedRegion> getRegions() {
        return regions;
    }

    public List<Waypoint> getWaypoints() {
        return waypoints;
    }

    public ProtectedRegion getRegion(String name) {
        for (ProtectedRegion region : regions) {
            if (region.getId().equals(name)) return region;
        }
        return null;
    }

    public Waypoint getWaypoint(String name) {
        for (Waypoint waypoint : waypoints) {
            if (waypoint.name().equals(name)) return waypoint;
        }
        return null;
    }

    public void teleportToWaypoint(Player player, Waypoint waypoint) {
        player.teleport(waypoint.location());
    }

    public void teleportToCenter(Player player) {
        player.teleport(new Location(world, 0.5, 0, 0.5));
    }
}
