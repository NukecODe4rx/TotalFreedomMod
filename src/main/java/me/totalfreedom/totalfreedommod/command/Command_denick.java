package me.totalfreedom.totalfreedommod.command;

import me.totalfreedom.totalfreedommod.rank.Rank;
import me.totalfreedom.totalfreedommod.util.FUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandPermissions(level = Rank.ADMIN, source = SourceType.BOTH)
@CommandParameters(description = "Essentials Interface Command - Remove the nickname of all players on the server.", usage = "/<command>")
public class Command_denick extends FreedomCommand
{

    @Override
    public boolean run(CommandSender sender, Player playerSender, Command cmd, String commandLabel, String[] args, boolean senderIsConsole)
    {
        if (!plugin.esb.isEnabled())
        {
            msg("Essentials is not enabled on this server.");
            return true;
        }

        FUtil.adminAction(sender.getName(), "Removing all nicknames", false);
        server.getOnlinePlayers().forEach(player -> plugin.esb.setNickname(player.getName(), null));
        return true;
    }
}
