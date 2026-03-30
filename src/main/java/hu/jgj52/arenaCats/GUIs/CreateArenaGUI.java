package hu.jgj52.arenaCats.GUIs;

import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.world.World;
import hu.jgj52.arenaCats.Listeners.ArenaEditorListener;
import hu.jgj52.arenaCats.Types.Arena;
import hu.jgj52.arenaCats.Types.ArenaEditor;
import hu.jgj52.arenaCats.Types.GUI;
import hu.jgj52.libCats.Listeners.ChatListener;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

import static hu.jgj52.arenaCats.ArenaCats.arenas;

public class CreateArenaGUI extends GUI {
    private Component name = getComponent("nameItemName", true);
    private boolean nameSet = false;
    private Clipboard arena;
    private boolean arenaSet = false;
    private ArenaEditor arenaEditor;
    private boolean settingName = false;
    @Override
    public void init(Player player) {
        ItemStack name = new ItemStack(Material.PAPER);
        ItemMeta nameMeta = name.getItemMeta();
        nameMeta.displayName(this.name);
        nameMeta.lore(List.of(getComponent(nameSet ? "itemLoreIfSet" : "itemLoreIfUnset", true)));
        name.setItemMeta(nameMeta);

        ItemStack arena = new ItemStack(Material.BRICKS);
        ItemMeta arenaMeta = arena.getItemMeta();
        arenaMeta.displayName(getComponent("arenaItemName", true));
        arenaMeta.lore(List.of(getComponent(arenaSet ? "itemLoreIfSet" : "itemLoreIfUnset", true)));
        arena.setItemMeta(arenaMeta);

        ItemStack save = new ItemStack(Material.GREEN_CONCRETE);
        ItemMeta saveMeta = save.getItemMeta();
        saveMeta.displayName(getComponent("saveItemName", true));
        save.setItemMeta(saveMeta);

        gui.setItem(12, name);
        gui.setItem(14, arena);
        gui.setItem(26, save);
    }

    @Override
    public void onClick(InventoryClickEvent event) {
        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player player)) return;
        switch (event.getSlot()) {
            case 12:
                settingName = true;
                player.closeInventory();
                player.sendMessage(getComponent("nameMessage"));
                ChatListener.add(player, message -> {
                    ConfigurationSection section = arenas.getConfig().getConfigurationSection("");
                    if (section != null) {
                        if (section.contains(PlainTextComponentSerializer.plainText().serialize(message))) {
                            player.sendMessage(getComponent("alreadyName"));
                            return;
                        }
                    }
                    name = message;
                    nameSet = true;
                    settingName = false;
                    open(player);
                });
                break;
            case 14:
                player.getInventory().clear();
                player.closeInventory();
                arenaEditor = new ArenaEditor(player, editor -> {
                    if (editor.getPlayerClipboard() == null) {
                        player.sendMessage(getComponent("noClipboard"));
                        return;
                    }
                    World clipboardWorld = editor.getPlayerClipboard().getRegion().getWorld();
                    if (clipboardWorld == null) {
                        player.sendMessage(getComponent("noClipboard"));
                        return;
                    }
                    if (!clipboardWorld.getName().equals(editor.getWorld().getName())) {
                        player.sendMessage(getComponent("noClipboard"));
                        return;
                    }
                    arena = editor.getPlayerClipboard();
                    arenaSet = true;
                    open(player);
                });
                arenaEditor.generate();
                arenaEditor.teleport(player.getLocation());
                arenaEditor.giveItems(player.getInventory().getContents());
                break;
            case 26:
                if (nameSet && arenaSet) {
                    player.closeInventory();
                    new Arena(PlainTextComponentSerializer.plainText().serialize(name), this.arena, arenaEditor);
                    player.sendMessage(getComponent("saved"));
                } else {
                    player.sendMessage(getComponent("notEverythingSet"));
                }
                break;
        }
    }

    @Override
    public void onClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) return;
        if (settingName) return;
        ArenaEditorListener.call(player, ArenaEditor::delete);
    }

    @Override
    public int getSize() {
        return 27;
    }
}
