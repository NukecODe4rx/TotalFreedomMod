package me.totalfreedom.totalfreedommod.command;

import me.totalfreedom.totalfreedommod.rank.Rank;
import me.totalfreedom.totalfreedommod.util.FUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandPermissions(level = Rank.ADMIN, source = SourceType.BOTH)
@CommandParameters(description = "Deop everyone on the server.", usage = "/<command>")
public class Command_deopall extends FreedomCommand
{

    @Override
    public boolean run(CommandSender sender, Player playerSender, Command cmd, String commandLabel, String[] args, boolean senderIsConsole)
    {
        FUtil.adminAction(sender.getName(), "De-opping all players on the server", true);

        for (Player player : server.getOnlinePlayers())
        {
            player.setOp(false);
            msg(player, YOU_ARE_NOT_OP);
            plugin.rm.updateDisplay(player);
        }

        return true;
    }
}
