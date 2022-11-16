package me.totalfreedom.totalfreedommod.command;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

import me.totalfreedom.totalfreedommod.player.PlayerData;
import me.totalfreedom.totalfreedommod.rank.Rank;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandPermissions(level = Rank.OP, source = SourceType.ONLY_IN_GAME)
@CommandParameters(description = "Block inspector tool for operators.", usage = "/<command> [history] [page]", aliases = "ins")
public class Command_inspect extends FreedomCommand
{
    @Override
    public boolean run(CommandSender sender, Player playerSender, Command cmd, String commandLabel, String[] args, boolean senderIsConsole)
    {
        if (!plugin.cpb.isEnabled())
        {
            msg("CoreProtect is not enabled on this server.");
            return true;
        }

        if (args.length == 0)
        {
            PlayerData playerData = plugin.pl.getData(playerSender);
            playerData.setInspect(!playerData.hasInspection());
            plugin.pl.save(playerData);
            msg("Block inspector " + (playerData.hasInspection() ? "enabled." : "disabled."));
            return true;
        }

        if (args[0].equalsIgnoreCase("history"))
        {
            int pageIndex = 1;

            if (args.length >= 2)
            {
                try
                {
                    pageIndex = Integer.parseInt(args[1]);
                }
                catch (NumberFormatException e)
                {
                    msg("Invalid number.", ChatColor.RED);
                    return true;
                }
            }

            int godDammit = pageIndex;
            Optional.ofNullable(plugin.cpb.getHistoryForPlayer(playerSender)).ifPresentOrElse(page ->
                    plugin.cpb.showPageToPlayer(playerSender, page, godDammit),
                    () -> msg("You haven't inspected anything yet!", ChatColor.RED));

            return true;
        }
        else
        {
            return false;
        }
    }

    @Override
    public List<String> getTabCompleteOptions(CommandSender sender, Command command, String alias, String[] args)
    {
        if (args.length == 1)
        {
            return Collections.singletonList("history");
        }
        else if (args.length == 2 && args[0].equalsIgnoreCase("history") && plugin.cpb.isEnabled()
                && sender instanceof Player player && plugin.cpb.hasHistory(player))
        {
            return IntStream.rangeClosed(1, plugin.cpb.getHistoryForPlayer(player).getPageCount()).limit(50)
                    .mapToObj(String::valueOf).toList();
        }
        else
        {
            return Collections.emptyList();
        }
    }
}