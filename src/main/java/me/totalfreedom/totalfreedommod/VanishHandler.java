package me.totalfreedom.totalfreedommod;

import me.totalfreedom.totalfreedommod.util.FLog;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class VanishHandler extends FreedomService
{
    @Override
    public void onStart()
    {
    }

    @Override
    public void onStop()
    {
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerJoin(PlayerJoinEvent event)
    {
        Player player = event.getPlayer();

        server.getOnlinePlayers().stream().filter(pl -> !plugin.al.isAdmin(player)
                && plugin.al.isVanished(pl.getUniqueId())).forEach(pl -> player.hidePlayer(plugin, pl));

        server.getOnlinePlayers().stream().filter(pl -> !plugin.al.isAdmin(pl)
                && plugin.al.isVanished(player.getUniqueId())).forEach(pl -> pl.hidePlayer(plugin, player));

        if (plugin.al.isVanished(player.getUniqueId()))
        {
            plugin.esb.setVanished(player.getName(), true);
            FLog.info(player.getName() + " joined while still vanished.");
            plugin.al.messageAllAdmins(ChatColor.YELLOW + player.getName() + " has joined silently.");
            event.joinMessage(null);

            new BukkitRunnable()
            {
                @Override
                public void run()
                {
                    if (plugin.al.isVanished(player.getUniqueId()))
                    {
                        player.sendActionBar(Component.text("You are hidden from other players.").color(NamedTextColor.GOLD));
                    }
                    else
                    {
                        this.cancel();
                    }
                }
            }.runTaskTimer(plugin, 0L, 4L);
        }
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event)
    {
        Player player = event.getPlayer();

        if (plugin.al.isVanished(player.getUniqueId()))
        {
            event.quitMessage(null);
            FLog.info(player.getName() + " left while still vanished.");
            plugin.al.messageAllAdmins(ChatColor.YELLOW + player.getName() + " has left silently.");
        }
    }
}