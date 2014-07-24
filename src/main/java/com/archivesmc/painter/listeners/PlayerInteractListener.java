package com.archivesmc.painter.listeners;

import com.archivesmc.painter.Painter;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Gareth Coles
 *
 * This is an event listener for the PlayerInteractEvent. This event is finnicky and
 * is cancelled by default when the player is left-clicking air, but we need it for
 * ranged-painting mode.
 */
public class PlayerInteractListener implements Listener {
    Painter plugin;

    public PlayerInteractListener(Painter plugin) {
        this.plugin = plugin;
    }

    /**
     * Handles the PlayerInteractEvent, which is used for ranged-painting mode.
     *
     * @param event The PlayerInteractEvent to handle
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerInteractEvent(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        if (this.plugin.isRangePainter(player.getUniqueId()) &&
                event.getAction() == Action.LEFT_CLICK_AIR
        ) {
            if (! this.plugin.hasPermission(player, "painter.replace.range")) {
                this.plugin.setRangePainter(player.getUniqueId(), false);

                Map<String, String> args = new HashMap<>();
                args.put("permission", "painter.replace.range");
                args.put("name", player.getName());

                this.plugin.sendMessage(player, "range_replace_perm_lost", args);
                return;
            }

            ItemStack items = player.getItemInHand();
            Material heldMat = items.getType();

            if (heldMat.isBlock()) {
                Block block = player.getTargetBlock(null, 100);

                if (block == null) {
                    return;
                }

                BlockState oldBlockState = block.getState();

                block.setType(heldMat);
                block.setData(items.getData().getData());

                event.setCancelled(true);

                // Log it if it's being logged
                this.plugin.blockPainted(player, oldBlockState, block.getState(), block);
            }
        }
    }
}
