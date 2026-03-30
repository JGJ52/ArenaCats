package hu.jgj52.arenaCats.GUIs;

import hu.jgj52.arenaCats.Types.Arena;
import hu.jgj52.arenaCats.Types.GUI;
import hu.jgj52.arenaCats.Types.PlacedArena;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static hu.jgj52.arenaCats.ArenaCats.arenas;
import static hu.jgj52.arenaCats.ArenaCats.plugin;

public class ArenaGUI extends GUI {
    private int page = 0;
    @Override
    public void init(Player player) {
        ConfigurationSection section = arenas.getConfig();
        if (section == null) return;
        List<String> list = new ArrayList<>(section.getKeys(false));
        Collections.sort(list);
        int start = page * 28;
        int end = Math.min(start + 28, list.size());
        int slot = 10;
        for (int j = 0; j < list.subList(start, end).size(); j++) {
            String name = list.get(start + j);
            Arena arena = Arena.of(name);
            if (arena == null) return;
            ItemStack icon = new ItemStack(Material.BRICKS);
            ItemMeta iconMeta = icon.getItemMeta();
            iconMeta.displayName(Component.text(arena.getName()).color(NamedTextColor.WHITE).decoration(TextDecoration.ITALIC, false));
            if (player.hasPermission("arenacats.command.arena.create")) {
                iconMeta.lore(List.of(getComponent("iconLore", true)));
            }
            iconMeta.getPersistentDataContainer().set(new NamespacedKey(plugin, "arena"), PersistentDataType.BOOLEAN, true);
            icon.setItemMeta(iconMeta);
            gui.setItem(slot, icon);
            slot++;
            if ((slot + 1) % 9 == 0) slot += 2;
        }
        ItemStack previous = new ItemStack(Material.ARROW);
        ItemMeta previousMeta = previous.getItemMeta();
        previousMeta.displayName(getComponent("previousArrow"));
        previous.setItemMeta(previousMeta);

        ItemStack next = new ItemStack(Material.ARROW);
        ItemMeta nextMeta = next.getItemMeta();
        nextMeta.displayName(getComponent("nextArrow"));
        next.setItemMeta(nextMeta);

        ItemStack create = new ItemStack(Material.BOOK);
        ItemMeta createMeta = create.getItemMeta();
        createMeta.displayName(getComponent("createArenaItemName", true));
        create.setItemMeta(createMeta);

        if (player.hasPermission("arenacats.command.arena.create")) {
            gui.setItem(4, create);
        }
        if (page > 0) {
            gui.setItem(45, previous);
        }
        if (end > 28) {
            gui.setItem(53, next);
        }
    }

    @Override
    public void onClick(InventoryClickEvent event) {
        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (event.getSlot() == 45) {
            if (page > 0) {
                page--;
                init(player);
            }
        } else if (event.getSlot() == 53) {
            ConfigurationSection section = arenas.getConfig();
            if (section == null) return;
            if ((section.getKeys(false).size() - 1) / 28 > page) {
                page++;
                init(player);
            }
        } else if (event.getSlot() == 4) {
            if (player.hasPermission("arenacats.command.arena.create")) {
                new CreateArenaGUI().open(player);
            }
        } else {
            if (event.getCurrentItem() == null) return;
            Component name = event.getCurrentItem().getItemMeta().displayName();
            if (name == null) return;
            if (event.getCurrentItem().getPersistentDataContainer().has(new NamespacedKey(plugin, "arena"))) {
                Arena arena = Arena.of(PlainTextComponentSerializer.plainText().serialize(name));
                if (arena == null) return;
                if (player.hasPermission("arenacats.command.arena.place")) {
                    PlacedArena placed = arena.place();
                    placed.teleportToCenter(player);
                }
            }
        }
    }

    @Override
    public int getSize() {
        return 54;
    }
}
