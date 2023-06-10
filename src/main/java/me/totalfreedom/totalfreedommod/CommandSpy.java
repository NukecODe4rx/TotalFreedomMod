package me.totalfreedom.totalfreedommod;

import me.totalfreedom.totalfreedommod.util.FUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class CommandSpy extends FreedomService
{
    @Override
    public void onStart()
    {
    }

    @Override
    public void onStop()
    {
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event)
    {
        server.getOnlinePlayers().stream().filter(player -> plugin.al.isAdmin(player)
                && plugin.al.getAdmin(player).getCommandSpy() && player != event.getPlayer())
                .forEach(player -> FUtil.playerMsg(player, event.getPlayer().getName() + ": " + event.getMessage()));
    }
}
