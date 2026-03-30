package hu.jgj52.arenaCats.Types;

import com.sk89q.worldedit.EmptyClipboardException;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import hu.jgj52.arenaCats.ArenaCats;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.io.File;
import java.util.*;
import java.util.function.Consumer;

import static hu.jgj52.arenaCats.ArenaCats.plugin;

public class ArenaEditor {
    private static final Map<UUID, ArenaEditor> editors = new HashMap<>();
    public static ArenaEditor getEditor(Player player) {
        return editors.get(player.getUniqueId());
    }
    private static String getMessage(String msg) {
        return ArenaCats.messages.getConfig().getString("arenaEditor." + msg);
    }

    private static Component getComponent(String msg) {
        return MiniMessage.miniMessage().deserialize(getMessage(msg));
    }

    private static Component getComponent(String msg, boolean notItalic) {
        Component component = getComponent(msg);
        if (!component.hasDecoration(TextDecoration.ITALIC) && notItalic) {
            return component.decoration(TextDecoration.ITALIC, false);
        }
        return component;
    }

    public static void deleteWorld(World world) {
        if (Bukkit.isTickingWorlds()) {
            Bukkit.getScheduler().runTaskLater(plugin, task -> deleteWorld(world), 20);
        } else {
            for (Player p : world.getPlayers()) {
                p.getInventory().clear();
                p.kick(Component.text("ArenaCats: World is resetting!"));
            }
            for (Chunk chunk : world.getForceLoadedChunks()) {
                world.setChunkForceLoaded(chunk.getX(), chunk.getZ(), false);
            }
            for (Entity entity : world.getEntities()) {
                if (!(entity instanceof Player)) entity.remove();
            }
            if (!Bukkit.unloadWorld(world, false)) {
                Bukkit.getScheduler().runTaskLater(plugin, task -> deleteWorld(world), 20);
                return;
            }
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> deleteFolder(new File(Bukkit.getWorldContainer(), world.getName())));
        }
    }

    private static void deleteFolder(File folder) {
        if (folder == null || !folder.exists()) return;
        File[] files = folder.listFiles();
        if (files != null) {
            for (File file : files) {
                deleteFolder(file);
            }
        }
        folder.delete();
    }

    private final Player player;
    private final Consumer<ArenaEditor> back;
    private final List<Waypoint> waypoints = new ArrayList<>();
    private World world;
    private Clipboard clipboard;
    private Location beforeLoc;
    private ItemStack[] beforeInv;
    public ArenaEditor(Player player, Consumer<ArenaEditor> back) {
        this.player = player;
        this.back = back;
    }
    
    public Player getPlayer() {
        return player;
    }
    
    public Clipboard getPlayerClipboard() {
        return clipboard;
    }

    public World getWorld() {
        return world;
    }

    @SuppressWarnings("removal")
    public void generate() {
        WorldCreator creator = new WorldCreator(UUID.randomUUID() + "_map_editor_arenacats");
        creator.generatorSettings("{\"layers\":[{\"block\":\"air\",\"height\":1}],\"biome\":\"plains\"}");
        creator.generateStructures(false);
        creator.type(WorldType.FLAT);
        world = creator.createWorld();
        if (world == null) return;
        world.setDifficulty(Difficulty.PEACEFUL);
        //stupid system but 26.1's naming is perfect for this
        String ver = Bukkit.getMinecraftVersion();
        if (ver.equals("1.21.11") || !ver.startsWith("1.")) {
            world.setGameRule(GameRules.SPAWN_MOBS, false);
            world.setGameRule(GameRules.ADVANCE_TIME, false);
            world.setGameRule(GameRules.ADVANCE_WEATHER, false);
        } else {
            world.setGameRule(GameRule.DO_MOB_SPAWNING, false);
            world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
            world.setGameRule(GameRule.DO_WEATHER_CYCLE, false);
        }
        world.setTime(6000);
        world.setWeatherDuration(0);
        Block block = world.getBlockAt(new Location(world, 0, -1, 0));
        block.setType(Material.BEDROCK);
    }
    
    public void teleport(Location location) {
        if (world == null) return;
        beforeLoc = location;
        player.teleport(new Location(world, 0.5, 0, 0.5));
        editors.put(player.getUniqueId(), this);
    }

    private ItemStack leave;

    public void giveItems(ItemStack[] inv) {
        beforeInv = inv;

        leave = new ItemStack(Material.BARRIER);
        ItemMeta leaveMeta = leave.getItemMeta();
        leaveMeta.displayName(getComponent("leaveItemName", true));
        leaveMeta.getPersistentDataContainer().set(new NamespacedKey(plugin, "leaveItem"), PersistentDataType.BOOLEAN, true);
        leave.setItemMeta(leaveMeta);

        player.getInventory().setItem(8, leave);
    }

    public void waypoint(String name) {
        waypoints.add(new Waypoint(name, player.getLocation()));
    }

    public List<Waypoint> getWaypoints() {
        return waypoints;
    }

    public void leave(boolean back) {
        if (back) {
            com.sk89q.worldedit.entity.Player wePlayer = BukkitAdapter.adapt(player);
            LocalSession session = WorldEdit.getInstance().getSessionManager().get(wePlayer);
            try {
                clipboard = session.getClipboard().getClipboard();
            } catch (EmptyClipboardException ignored) {}
            this.back.accept(this);
        } else {
            delete();
        }
    }

    public void delete() {
        editors.remove(player.getUniqueId());
        player.teleport(beforeLoc);
        player.getInventory().setContents(beforeInv);
        Bukkit.getScheduler().runTaskLater(plugin, () -> deleteWorld(world), 10);
    }

    public void onInteract() {
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item.isSimilar(leave)) {
            leave(true);
        }
    }

    public void onLeave() {
        leave(false);
    }

    public void onTeleport(PlayerTeleportEvent event) {
        Location to = event.getTo();
        if (to.getWorld().getName().equals(world.getName())) return;
        leave(false);
    }
}
