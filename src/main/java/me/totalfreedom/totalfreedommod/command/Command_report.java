package me.totalfreedom.totalfreedommod.command;

import me.totalfreedom.totalfreedommod.rank.Rank;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

@CommandPermissions(level = Rank.OP, source = SourceType.ONLY_IN_GAME, blockHostConsole = true)
@CommandParameters(description = "Report a player for all admins to see.", usage = "/<command> <player> <reason>")
public class Command_report extends FreedomCommand
{
    private void handleLog(final @Nullable Boolean value, final @Nullable Throwable ex, final CommandSender sender)
    {
        if (ex != null)
        {
            sender.sendMessage(Component.text("An error occurred while attempting to log your previously filed report to a Discord channel.", NamedTextColor.RED));
            ex.printStackTrace();
            return;
        }

        if (Boolean.FALSE.equals(value))
        {
            return;
        }

        sender.sendMessage(Component.text("The report you previously filed has been successfully logged to a Discord channel. Please note that spamming reports is not allowed, and you will be sanctioned if you are found to be doing it.", NamedTextColor.GRAY));
    }

    @Override
    public boolean run(CommandSender sender, Player playerSender, Command cmd, String commandLabel, String[] args, boolean senderIsConsole)
    {
        if (args.length < 2)
        {
            return false;
        }

        Player player = getPlayer(args[0], true);
        OfflinePlayer offlinePlayer = getOfflinePlayer(args[0]);

        if (player == null && offlinePlayer == null)
        {
            msg(PLAYER_NOT_FOUND);
            return true;
        }
        else if (player != null)
        {
            if (sender instanceof Player)
            {
                if (player.equals(playerSender))
                {
                    msg(ChatColor.RED + "Please, don't try to report yourself.");
                    return true;
                }
            }

            if (plugin.al.isAdmin(player))
            {
                msg(ChatColor.RED + "You can not report admins.");
                return true;
            }

        }

        String report = StringUtils.join(ArrayUtils.subarray(args, 1, args.length), " ");
        String reportedUsername = (player == null) ? offlinePlayer.getName() : player.getName();
        plugin.cm.reportAction(playerSender, reportedUsername, report);

        msg(ChatColor.GREEN + "Thank you, your report is being processed.");

        if (plugin.dc.enabled)
        {
            plugin.dc.sendReport(playerSender.getName(), reportedUsername, report).whenCompleteAsync((logged, ex) -> handleLog(logged, ex, sender));
        }

        return true;
    }
}