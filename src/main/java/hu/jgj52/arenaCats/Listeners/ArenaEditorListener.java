package hu.jgj52.arenaCats.Listeners;

import hu.jgj52.arenaCats.Types.ArenaEditor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.function.Consumer;

public class ArenaEditorListener implements Listener {
    public static void call(Player player, Consumer<ArenaEditor> consumer) {
        ArenaEditor editor = ArenaEditor.getEditor(player);
        if (editor == null) return;
        consumer.accept(editor);
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        call(event.getPlayer(), ArenaEditor::onInteract);
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent event) {
        call(event.getPlayer(), ArenaEditor::onLeave);
    }

    @EventHandler
    public void onTeleport(PlayerTeleportEvent event) {
        call(event.getPlayer(), editor -> editor.onTeleport(event));
    }
}
