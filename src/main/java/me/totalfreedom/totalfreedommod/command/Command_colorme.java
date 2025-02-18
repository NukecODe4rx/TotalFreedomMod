package me.totalfreedom.totalfreedommod.command;

import java.util.Map;
import me.totalfreedom.totalfreedommod.rank.Rank;
import me.totalfreedom.totalfreedommod.util.FUtil;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandPermissions(level = Rank.OP, source = SourceType.ONLY_IN_GAME)
@CommandParameters(description = "Essentials Interface Command - Set your nickname to a certain color.", usage = "/<command> <color>")
public class Command_colorme extends FreedomCommand
{

    @Override
    public boolean run(CommandSender sender, Player playerSender, Command cmd, String commandLabel, String[] args, boolean senderIsConsole)
    {
        if (!plugin.esb.isEnabled())
        {
            msg("Essentials is not enabled on this server.");
            return true;
        }

        if (args.length != 1)
        {
            return false;
        }

        if (args[0].equalsIgnoreCase("list"))
        {
            msg("Colors: " + StringUtils.join(FUtil.CHAT_COLOR_NAMES.keySet(), ", "));
            return true;
        }

        final String needle = args[0].trim().toLowerCase();
        ChatColor color = null;
        for (Map.Entry<String, ChatColor> entry : FUtil.CHAT_COLOR_NAMES.entrySet())
        {
            if (entry.getKey().contains(needle))
            {
                color = entry.getValue();
                break;
            }
        }

        if (color == null)
        {
            msg("Invalid color: " + needle + " - Use \"/colorme list\" to list colors.");
            return true;
        }

        final String newNick = color + ChatColor.stripColor(playerSender.getDisplayName()).trim() + ChatColor.WHITE;

        plugin.esb.setNickname(sender.getName(), newNick);

        msg("Your nickname is now: " + newNick);
        return true;
    }
}