package me.totalfreedom.totalfreedommod.command;

import me.totalfreedom.totalfreedommod.rank.Rank;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.generator.WorldInfo;

import java.util.List;

@CommandPermissions(level = Rank.NON_OP, source = SourceType.BOTH)
@CommandParameters(description = "Get the seed of the world you are currently in.", usage = "/seed [world]")
public class Command_seed extends FreedomCommand
{
    @Override
    public boolean run(CommandSender sender, Player playerSender, Command cmd, String commandLabel, String[] args, boolean senderIsConsole)
    {
        World world;

        if (args.length > 0)
        {
            world = server.getWorld(args[0]);
            if (world == null)
            {
                msg("That world could not be found", ChatColor.RED);
                return true;
            }
        }
        else
        {
            // If the sender is a Player, use that world. Otherwise, use the overworld as a fallback.
            if (!senderIsConsole)
            {
                world = playerSender.getWorld();
            }
            else
            {
                world = server.getWorlds().get(0);
            }
        }

        sender.sendMessage(Component.translatable("commands.seed.success",
                Component.text("[", NamedTextColor.WHITE).append(Component.text(world.getSeed(), NamedTextColor.GREEN)
                                .clickEvent(ClickEvent.copyToClipboard(String.valueOf(world.getSeed())))
                                .hoverEvent(HoverEvent.showText(Component.translatable("chat.copy"))))
                        .append(Component.text("]"))));
        return true;
    }

    @Override
    public List<String> getTabCompleteOptions(CommandSender sender, Command command, String alias, String[] args)
    {
        if (args.length == 1)
        {
            return server.getWorlds().stream().map(WorldInfo::getName).toList();
        }

        return null;
    }
}