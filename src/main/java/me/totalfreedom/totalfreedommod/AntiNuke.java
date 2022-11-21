package me.totalfreedom.totalfreedommod;

import me.totalfreedom.totalfreedommod.config.ConfigEntry;
import me.totalfreedom.totalfreedommod.player.FPlayer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

public class AntiNuke extends FreedomService
{
    @Override
    public void onStart()
    {
    }

    @Override
    public void onStop()
    {
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event)
    {
        if (!ConfigEntry.NUKE_MONITOR_ENABLED.getBoolean())
        {
            return;
        }

        final Player player = event.getPlayer();
        final FPlayer fPlayer = plugin.pl.getPlayer(player);

        if (fPlayer.incrementAndGetBlockDestroyCount() > ConfigEntry.NUKE_MONITOR_COUNT_BREAK.getInteger())
        {
            server.broadcast(Component.text(player.getName()).append(Component.text(" is breaking blocks too fast!"))
                    .color(NamedTextColor.RED));
            player.kick(Component.text("You are breaking blocks too fast. Nukers are not permitted on this server.",
                    NamedTextColor.RED));
            fPlayer.resetBlockDestroyCount();

            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockPlace(BlockPlaceEvent event)
    {
        if (!ConfigEntry.NUKE_MONITOR_ENABLED.getBoolean())
        {
            return;
        }

        Player player = event.getPlayer();
        FPlayer fPlayer = plugin.pl.getPlayer(player);

        if (fPlayer.incrementAndGetBlockPlaceCount() > ConfigEntry.NUKE_MONITOR_COUNT_PLACE.getInteger())
        {
            server.broadcast(Component.text(player.getName()).append(Component.text(" is placing blocks too fast!"))
                    .color(NamedTextColor.RED));
            player.kick(Component.text("You are placing blocks too fast.", NamedTextColor.RED));
            fPlayer.resetBlockPlaceCount();

            event.setCancelled(true);
        }
    }
}
