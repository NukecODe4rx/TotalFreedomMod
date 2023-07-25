package me.totalfreedom.totalfreedommod.command;

import me.totalfreedom.totalfreedommod.rank.Rank;
import me.totalfreedom.totalfreedommod.util.FUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandPermissions(level = Rank.ADMIN, source = SourceType.BOTH)
@CommandParameters(description = "Kick all non-admins on server.", usage = "/<command>", aliases = "kickall")
public class Command_kicknoob extends FreedomCommand
{
    @Override
    public boolean run(CommandSender sender, Player playerSender, Command cmd, String commandLabel, String[] args, boolean senderIsConsole)
    {
        FUtil.adminAction(sender.getName(), "Disconnecting all non-admins", true);

        server.getOnlinePlayers().stream().filter(player -> !plugin.al.isAdmin(player)).forEach(player ->
                player.kick(Component.text("All non-admins were kicked by " + sender.getName() + ".", NamedTextColor.RED)));
        return true;
    }
}