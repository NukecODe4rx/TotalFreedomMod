package me.totalfreedom.totalfreedommod.command;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import me.totalfreedom.totalfreedommod.rank.Rank;
import me.totalfreedom.totalfreedommod.util.FUtil;
import org.apache.commons.lang.StringUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

@CommandPermissions(level = Rank.OP, source = SourceType.BOTH)
@CommandParameters(description = "Shows (optionally clears) invisible players", usage = "/<command> [clear]")
public class Command_invis extends FreedomCommand
{

    @Override
    public boolean run(CommandSender sender, Player playerSender, Command cmd, String commandLabel, String[] args, boolean senderIsConsole)
    {
        boolean clear = false;

        if (args.length >= 1)
        {
            if (args[0].equalsIgnoreCase("clear"))
            {
                if (!plugin.al.isAdmin(sender))
                {
                    return noPerms();
                }
                else
                {
                    FUtil.adminAction(sender.getName(), "Clearing all invisibility potion effects from all players", true);
                    clear = true;
                }
            }
            else
            {
                return false;
            }
        }

        List<String> players = new ArrayList<>();
        int clears = 0;

        for (Player player : server.getOnlinePlayers())
        {
            if (player.hasPotionEffect(PotionEffectType.INVISIBILITY) && !plugin.al.isVanished(player.getUniqueId()))
            {
                players.add(player.getName());
                if (clear && !plugin.al.isAdmin(player))
                {
                    player.removePotionEffect((PotionEffectType.INVISIBILITY));
                    clears++;
                }
            }
        }

        if (players.isEmpty())
        {
            msg("There are no invisible players");
            return true;
        }

        if (clear)
        {
            msg("Cleared " + clears + " players");
        }
        else
        {
            msg("Invisible players (" + players.size() + "): " + StringUtils.join(players, ", "));
        }

        return true;
    }

    @Override
    public List<String> getTabCompleteOptions(CommandSender sender, Command command, String alias, String[] args)
    {
        if (args.length == 1 && plugin.al.isAdmin(sender))
        {
            return Collections.singletonList("clear");
        }

        return Collections.emptyList();
    }
}