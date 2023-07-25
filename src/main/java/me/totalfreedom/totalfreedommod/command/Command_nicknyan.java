package me.totalfreedom.totalfreedommod.command;

import me.totalfreedom.totalfreedommod.rank.Rank;
import me.totalfreedom.totalfreedommod.util.FUtil;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import java.util.Arrays;

@CommandPermissions(level = Rank.OP, source = SourceType.ONLY_IN_GAME)
@CommandParameters(description = "Essentials Interface Command - Randomize the colors of your nickname.", usage = "/<command> <<nick> | off>")
public class Command_nicknyan extends FreedomCommand
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

        if (args[0].equalsIgnoreCase("off"))
        {
            plugin.esb.setNickname(sender.getName(), null);
            msg("Nickname cleared.");
            return true;
        }

        final String nickPlain = ChatColor.stripColor(FUtil.colorize(args[0].trim()));

        if (!nickPlain.matches("^[a-zA-Z_0-9" + ChatColor.COLOR_CHAR + "]+$"))
        {
            msg("That nickname contains invalid characters.");
            return true;
        }
        else if (nickPlain.length() < 3 || nickPlain.length() > 30)
        {
            msg("Your nickname must be between 3 and 30 characters long.");
            return true;
        }


        if (server.getOnlinePlayers().stream().anyMatch(player -> player != playerSender
                && (player.getName().equalsIgnoreCase(nickPlain)
                || ChatColor.stripColor(plugin.esb.getNickname(player.getName())).trim().equalsIgnoreCase(nickPlain))))
        {
            msg("That nickname is already in use.");
            return true;
        }

        final StringBuilder newNick = new StringBuilder();
        Arrays.stream(nickPlain.chars().toArray()).forEach(character -> newNick.append(FUtil.randomChatColor())
                .append(Character.toString(character)));

        newNick.append(ChatColor.WHITE);

        plugin.esb.setNickname(sender.getName(), newNick.toString());

        msg("Your nickname is now: " + newNick);
        return true;
    }
}