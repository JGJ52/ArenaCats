package hu.jgj52.arenaCats.Types;

import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardWriter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import net.kyori.adventure.key.Key;
import org.bukkit.Bukkit;
import org.bukkit.Difficulty;
import org.bukkit.GameRule;
import org.bukkit.Registry;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static hu.jgj52.arenaCats.ArenaCats.arenas;
import static hu.jgj52.arenaCats.ArenaCats.plugin;

public class Arena {
    public static Arena of(String name) {
        ConfigurationSection section = arenas.getConfig().getConfigurationSection(name);
        if (section == null) return null;
        return new Arena(section);
    }

    private String name;

    public Arena(String name, Clipboard clipboard, ArenaEditor editor) {
        File file = new File(plugin.getDataFolder(), "schematics" + File.separator + name + ".schem");
        file.getParentFile().mkdirs();
        try (ClipboardWriter writer = ClipboardFormats.findByAlias("schem").getWriter(new FileOutputStream(file))) {
            writer.write(clipboard);
        } catch (Exception e) {
            e.printStackTrace();
        }
        ConfigurationSection arena = arenas.getConfig().getConfigurationSection(name);
        if (arena == null) arena = arenas.getConfig().createSection(name);

        Region region = clipboard.getRegion();
        BlockVector3 min1 = clipboard.getMinimumPoint();
        BlockVector3 max1 = clipboard.getMaximumPoint();
        int centerX = clipboard.getOrigin().x();
        int centerY = clipboard.getOrigin().y();
        int centerZ = clipboard.getOrigin().z();

        Plugin wg = Bukkit.getPluginManager().getPlugin("WorldGuard");
        if (wg != null && wg.isEnabled()) {
            RegionManager rm = WorldGuard.getInstance().getPlatform().getRegionContainer().get(region.getWorld());
            if (rm != null) {
                List<ProtectedRegion> regions = new ArrayList<>();
                for (ProtectedRegion rg : rm.getRegions().values()) {
                    BlockVector3 min2 = rg.getMinimumPoint();
                    BlockVector3 max2 = rg.getMaximumPoint();
                    if (min1.x() <= max2.x() && max1.x() >= min2.x() &&
                            min1.y() <= max2.y() && max1.y() >= min2.y() &&
                            min1.z() <= max2.z() && max1.z() >= min2.z()) {
                        regions.add(rg);
                    }
                }
                for (ProtectedRegion r : regions) {
                    Map<Flag<?>, Object> flags = r.getFlags();
                    Map<String, Object> savableFlags = new HashMap<>();
                    for (Flag<?> flag : flags.keySet()) {
                        Object value = flags.get(flag);
                        if (value instanceof StateFlag.State state) {
                            value = state.name();
                        }
                        savableFlags.put(flag.getName(), value);
                    }
                    BlockVector3 minimumPoint = r.getMinimumPoint();
                    BlockVector3 maximumPoint = r.getMaximumPoint();
                    arena.set("regions." + r.getId() + ".x1", minimumPoint.x() - centerX);
                    arena.set("regions." + r.getId() + ".y1", minimumPoint.y() - centerY);
                    arena.set("regions." + r.getId() + ".z1", minimumPoint.z() - centerZ);
                    arena.set("regions." + r.getId() + ".x2", maximumPoint.x() - centerX);
                    arena.set("regions." + r.getId() + ".y2", maximumPoint.y() - centerY);
                    arena.set("regions." + r.getId() + ".z2", maximumPoint.z() - centerZ);
                    arena.set("regions." + r.getId() + ".flags", savableFlags);
                    arena.set("regions." + r.getId() + ".priority", r.getPriority());
                }
            }
        }
        for (Waypoint waypoint : editor.getWaypoints()) {
            if (waypoint.x() >= min1.x() && waypoint.x() <= max1.x() &&
                waypoint.y() >= min1.y() && waypoint.y() <= max1.y() &&
                waypoint.z() >= min1.z() && waypoint.z() <= max1.z()) {
                arena.set("waypoints." + waypoint.name() + ".x", waypoint.x() - centerX);
                arena.set("waypoints." + waypoint.name() + ".y", waypoint.y() - centerY);
                arena.set("waypoints." + waypoint.name() + ".z", waypoint.z() - centerZ);
                arena.set("waypoints." + waypoint.name() + ".yaw", waypoint.yaw());
                arena.set("waypoints." + waypoint.name() + ".pitch", waypoint.pitch());
            }
        }
        Map<String, Object> gamerules = new HashMap<>();
        String ver = Bukkit.getMinecraftVersion();
        if (ver.equals("1.21.11") || !ver.startsWith("1.")) {
            for (String gamerule : editor.getWorld().getGameRules()) {
                Registry<GameRule<?>> registry = RegistryAccess.registryAccess().getRegistry(RegistryKey.GAME_RULE);
                GameRule<?> rule = registry.get(RegistryKey.GAME_RULE.typedKey(Key.key("minecraft:" + gamerule)));
                gamerules.put(gamerule, editor.getWorld().getGameRuleValue(rule));
            }
        } else {
            for (String gamerule : editor.getWorld().getGameRules()) {
                gamerules.put(gamerule, editor.getWorld().getGameRuleValue(gamerule));
            }
        }
        arena.set("gamerules", gamerules);
        arena.set("difficulty", editor.getWorld().getDifficulty().name());
        arenas.saveConfig();
        arenas.reloadConfig();
        editor.delete();
        construct(arena);
    }

    private Arena(ConfigurationSection section) {
        construct(section);
    }

    private void construct(ConfigurationSection section) {
        this.name = section.getName();
    }

    public String getName() {
        return name;
    }

    public PlacedArena place() {
        File file = new File(plugin.getDataFolder(), "schematics" + File.separator + name + ".schem");
        if (!file.exists()) return null;
        Clipboard clipboard;
        try (FileInputStream fis = new FileInputStream(file)) {
            ClipboardReader reader = ClipboardFormats.findByAlias("schem").getReader(fis);
            clipboard = reader.read();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return new PlacedArena(
                clipboard,
                arenas.getConfig().getConfigurationSection(name + ".regions"),
                arenas.getConfig().getConfigurationSection(name + ".waypoints"),
                arenas.getConfig().getConfigurationSection(name + ".gamerules"),
                Difficulty.valueOf(arenas.getConfig().getString(name + ".difficulty")) != null ? Difficulty.valueOf(arenas.getConfig().getString(name + ".difficulty")) : Difficulty.HARD
        );
    }

    public void delete() {
        arenas.getConfig().set(name, null);
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            File file = new File(plugin.getDataFolder(), "schematics" + File.separator + name + ".schem");
            if (!file.exists()) return;
            file.delete();
        });
    }
}
