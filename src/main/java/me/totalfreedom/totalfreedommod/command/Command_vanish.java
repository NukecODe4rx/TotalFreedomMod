package me.totalfreedom.totalfreedommod.command;

import me.totalfreedom.totalfreedommod.admin.AdminList;
import me.totalfreedom.totalfreedommod.player.PlayerData;
import me.totalfreedom.totalfreedommod.rank.Displayable;
import me.totalfreedom.totalfreedommod.rank.Rank;
import me.totalfreedom.totalfreedommod.util.FLog;
import me.totalfreedom.totalfreedommod.util.FUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

@CommandPermissions(level = Rank.ADMIN, source = SourceType.ONLY_IN_GAME)
@CommandParameters(description = "Vanish/unvanish yourself.", usage = "/<command> [-s[ilent]]", aliases = "v")
public class Command_vanish extends FreedomCommand
{
    @Override
    public boolean run(final CommandSender sender, final Player playerSender, final Command cmd, final String commandLabel, final String[] args, final boolean senderIsConsole)
    {
        Displayable display = plugin.rm.getDisplay(playerSender);
        String displayName = display.getColor() + playerSender.getName();
        String tag = display.getColoredTag();
        boolean silent = false;
        if (args.length > 0)
        {
            if (args[0].equalsIgnoreCase("-s") || args[0].equalsIgnoreCase("-silent"))
            {
                silent = true;
            }
        }

        if (plugin.al.isVanished(playerSender.getUniqueId()))
        {
            if (silent)
            {
                msg(ChatColor.GOLD + "Silently unvanished.");
            }
            else
            {
                msg("You have unvanished.", ChatColor.GOLD);
                FUtil.bcastMsg(plugin.rm.craftLoginMessage(playerSender, null));
                server.broadcast(Component.translatable("multiplayer.player.joined", Component.text(playerSender.getName()))
                        .color(NamedTextColor.YELLOW));
                plugin.dc.messageChatChannel("**" + playerSender.getName() + " joined the server" + "**", true);
            }

            PlayerData playerData = plugin.pl.getData(playerSender);
            if (playerData.getTag() != null)
            {
                tag = FUtil.colorize(playerData.getTag());
            }

            plugin.pl.getData(playerSender).setTag(tag);
            FLog.info(playerSender.getName() + " is no longer vanished.");
            plugin.al.messageAllAdmins(ChatColor.YELLOW + sender.getName() + " has unvanished and is now visible to everyone.");

            for (Player player : server.getOnlinePlayers())
            {
                if (!plugin.al.isAdmin(player))
                {
                    player.showPlayer(plugin, playerSender);
                }
            }
            plugin.esb.setVanished(playerSender.getName(), false);
            playerSender.setPlayerListName(StringUtils.substring(displayName, 0, 16));
            AdminList.vanished.remove(playerSender.getUniqueId());
        }
        else
        {
            new BukkitRunnable()
            {
                @Override
                public void run()
                {
                    if (plugin.al.isVanished(playerSender.getUniqueId()))
                    {
                        sender.sendActionBar(Component.text("You are hidden from other players.").color(NamedTextColor.GOLD));
                    }
                    else
                    {
                        cancel();
                    }
                }
            }.runTaskTimer(plugin, 0L, 4L);

            if (silent)
            {
                msg("Silently vanished.", ChatColor.GOLD);
            }
            else
            {
                msg("You have vanished.", ChatColor.GOLD);
                server.broadcast(Component.translatable("multiplayer.player.left", Component.text(playerSender.getName()))
                        .color(NamedTextColor.YELLOW));
                plugin.dc.messageChatChannel("**" + playerSender.getName() + " left the server" + "**", true);
            }

            FLog.info(playerSender.getName() + " is now vanished.");
            plugin.al.messageAllAdmins(ChatColor.YELLOW + sender.getName() + " has vanished and is now only visible to admins.");

            server.getOnlinePlayers().stream().filter(player -> !plugin.al.isAdmin(player)).forEach(player ->
                    player.hidePlayer(plugin,playerSender));

            plugin.esb.setVanished(playerSender.getName(), true);
            AdminList.vanished.add(playerSender.getUniqueId());
        }
        return true;
    }
}