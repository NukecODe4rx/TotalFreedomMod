package me.totalfreedom.totalfreedommod.bridge;

import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.LibsDisguises;
import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import me.libraryaddict.disguise.disguisetypes.PlayerDisguise;
import me.libraryaddict.disguise.disguisetypes.watchers.*;
import me.libraryaddict.disguise.events.DisguiseEvent;
import me.totalfreedom.totalfreedommod.FreedomService;
import me.totalfreedom.totalfreedommod.util.FLog;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.plugin.Plugin;

public class LibsDisguisesBridge extends FreedomService
{
    private LibsDisguises libsDisguisesPlugin = null;
    private boolean disguisesEnabled = true;

    @Override
    public void onStart()
    {
    }

    @Override
    public void onStop()
    {
    }

    public LibsDisguises getLibsDisguisesPlugin()
    {
        if (libsDisguisesPlugin == null)
        {
            try
            {
                final Plugin libsDisguises = server.getPluginManager().getPlugin("LibsDisguises");
                if (libsDisguises != null)
                {
                    if (libsDisguises instanceof LibsDisguises)
                    {
                        libsDisguisesPlugin = (LibsDisguises)libsDisguises;
                    }
                }
            }
            catch (Exception ex)
            {
                FLog.severe(ex);
            }
        }

        return libsDisguisesPlugin;
    }

    public void undisguiseAll(boolean admin)
    {
        try
        {
            final LibsDisguises libsDisguises = getLibsDisguisesPlugin();

            if (libsDisguises == null)
            {
                return;
            }

            for (Player player : server.getOnlinePlayers())
            {
                if (DisguiseAPI.isDisguised(player))
                {
                    if (!admin && plugin.al.isAdmin(player))
                    {
                        continue;
                    }
                    DisguiseAPI.undisguiseToAll(player);
                }
            }
        }
        catch (Exception ex)
        {
            FLog.severe(ex);
        }
    }

    public boolean isDisguisesEnabled()
    {
        return this.disguisesEnabled;
    }

    public void setDisguisesEnabled(boolean state)
    {
        this.disguisesEnabled = state;
    }

    public boolean isEnabled()
    {
        final LibsDisguises libsDisguises = getLibsDisguisesPlugin();
        return libsDisguises != null;
    }

    private static float safeYMod(float f) {
        return Math.max(-256f, Math.min(256f, f));
    }

    // Most of this code was taken from https://github.com/ayunami2000/LiberalDisguises with permission.
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onDisguiseEvent(final DisguiseEvent event) {
        if (!this.disguisesEnabled) {
            event.getCommandSender().sendMessage(Component.text("Disguises are disabled.", NamedTextColor.RED));
            event.setCancelled(true);
            return;
        }

        if (event.getDisguise().getType() == DisguiseType.FISHING_HOOK) {
            event.getCommandSender().sendMessage(Component.text("You cannot use Fishing Hook disguises", NamedTextColor.RED));
            event.setCancelled(true);
            return;
        }

        if (event.getDisguise().getWatcher() instanceof EnderDragonWatcher watcher && watcher.getPhase() == 7) watcher.setPhase(6);
        if (event.getDisguise().getWatcher() instanceof WitherWatcher watcher && watcher.getInvulnerability() > 2048) watcher.setInvulnerability(2048);
        if (event.getDisguise().isPlayerDisguise()) {
            PlayerDisguise playerDisguise = (PlayerDisguise) event.getDisguise();
            String targetName = playerDisguise.getName();
            String origName = event.getDisguised().getName();
            playerDisguise.setName(origName);
            playerDisguise.setNameVisible(true);
            playerDisguise.getWatcher().setNameYModifier(0);
            playerDisguise.setSkin(targetName);
            playerDisguise.setDisplayedInTab(false);
            playerDisguise.setTablistName(origName);
        }
        if (event.getDisguise().isHidePlayer()) event.getDisguise().setHidePlayer(false);

        if (event.getDisguise().getWatcher() instanceof AreaEffectCloudWatcher watcher) {
            if (watcher.getRadius() > 5) {
                watcher.setRadius(5);
            } else if (watcher.getRadius() < 0) {
                watcher.setRadius(0);
            }
        }

        event.getDisguise().getWatcher().setNameYModifier(safeYMod(event.getDisguise().getWatcher().getNameYModifier()));
        event.getDisguise().getWatcher().setYModifier(safeYMod(event.getDisguise().getWatcher().getYModifier()));
        if (event.getDisguise().getWatcher() instanceof SlimeWatcher watcher && watcher.getSize() > 10)
            watcher.setSize(10);
        if (event.getDisguise().getWatcher() instanceof PhantomWatcher watcher) {
            if (watcher.getSize() > 20) {
                watcher.setSize(20);
            } else if (watcher.getSize() < -36) {
                watcher.setSize(-36);
            }
        }

    }
}