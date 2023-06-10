package me.totalfreedom.totalfreedommod.command;

import me.totalfreedom.totalfreedommod.rank.Rank;
import me.totalfreedom.totalfreedommod.util.FUtil;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandPermissions(level = Rank.OP, source = SourceType.ONLY_IN_GAME)
@CommandParameters(description = "Go to a random location in the current world you are in", usage = "/<command>", aliases = "tpr,rtp")
public class Command_tprandom extends FreedomCommand
{
    @Override
    public boolean run(CommandSender sender, Player playerSender, Command cmd, String commandLabel, String[] args, boolean senderIsConsole)
    {
        final int x = FUtil.randomInteger(-50000, 50000);
        final int z = FUtil.randomInteger(-50000, 50000);
        final World world = playerSender.getWorld();
        Location location = new Location(playerSender.getLocation().getWorld(), x, 0, z);

        server.getScheduler().runTaskAsynchronously(plugin, () -> world.getChunkAtAsync(location).whenCompleteAsync((chunk, throwable) -> {
            if (throwable != null) {
                throwable.printStackTrace();
                return;
            }

            location.setY(playerSender.getWorld().getHighestBlockYAt(x, z));
            playerSender.teleportAsync(location);
            msg("Poof!", ChatColor.GREEN);
        }));
        return true;
    }
}
