package me.totalfreedom.totalfreedommod.command;

import me.totalfreedom.totalfreedommod.admin.Admin;
import me.totalfreedom.totalfreedommod.rank.Rank;
import me.totalfreedom.totalfreedommod.util.FUtil;
import net.md_5.bungee.api.ChatColor;
import org.apache.commons.lang.StringUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

@CommandPermissions(level = Rank.NON_OP, source = SourceType.BOTH)
@CommandParameters(description = "Lists the real names of all online players.", usage = "/<command> [-a | -v]", aliases = "who,lsit")
public class Command_list extends FreedomCommand
{
    @Override
    public boolean run(final CommandSender sender, final Player playerSender, final Command cmd, final String commandLabel, final String[] args, final boolean senderIsConsole)
    {
        if (args.length > 1)
        {
            return false;
        }

        ListFilter listFilter;
        if (args.length == 1)
        {
            switch (args[0].toLowerCase())
            {
                case "-s", "-a" -> listFilter = ListFilter.ADMINS;
                case "-v" ->
                {
                    checkRank(Rank.ADMIN);
                    listFilter = ListFilter.VANISHED_ADMINS;
                }
                case "-t" ->
                {
                    checkRank(Rank.ADMIN);
                    listFilter = ListFilter.TELNET_SESSIONS;
                }
                default ->
                {
                    return false;
                }
            }
        }
        else
        {
            listFilter = ListFilter.PLAYERS;
        }

        String onlineStats;
        List<String> players;

        if (listFilter == ListFilter.TELNET_SESSIONS && plugin.al.isAdmin(sender))
        {
            players = plugin.btb.getConnectedAdmins().stream().map(Admin::getName).toList();
            onlineStats = ChatColor.BLUE + "There are " + ChatColor.RED + players.size() + ChatColor.BLUE
                    + " admins connected to telnet.";
        }
        else
        {
            onlineStats = ChatColor.BLUE + "There are " + ChatColor.RED + FUtil.getFakePlayerCount() + ChatColor.BLUE
                    + " out of a maximum " + ChatColor.RED + server.getMaxPlayers() + ChatColor.BLUE + " players online.";

            players = server.getOnlinePlayers().stream().filter(pl ->
                    (listFilter == ListFilter.ADMINS && plugin.al.isAdmin(pl) && !plugin.al.isVanished(pl.getUniqueId()))
                    || (listFilter == ListFilter.VANISHED_ADMINS && plugin.al.isVanished(pl.getUniqueId()))
                    || (listFilter == ListFilter.PLAYERS && !plugin.al.isVanished(pl.getUniqueId()))).map(player ->
                    plugin.rm.getDisplay(player).getColoredTag() + player.getName()).toList();
        }

        String onlineUsers = "Connected " + listFilter.name().toLowerCase().replace('_', ' ') + ": " + ChatColor.WHITE +
                StringUtils.join(players, ChatColor.WHITE + ", " + ChatColor.WHITE);

        if (senderIsConsole)
        {
            msg(ChatColor.stripColor(onlineStats));
            msg(ChatColor.stripColor(onlineUsers));
        }
        else
        {
            msg(onlineStats);
            msg(onlineUsers);
        }

        return true;
    }

    private enum ListFilter
    {
        PLAYERS,
        ADMINS,
        VANISHED_ADMINS,
        TELNET_SESSIONS
    }
}