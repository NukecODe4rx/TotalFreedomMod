package me.totalfreedom.totalfreedommod.command;

import java.util.Optional;
import me.totalfreedom.totalfreedommod.rank.Rank;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Switch;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandPermissions(level = Rank.NON_OP, source = SourceType.BOTH)
@CommandParameters(description = "Set the on/off state of the lever at position x, y, z in world 'worldname'.", usage = "/<command> <x> <y> <z> <worldname> <on | off>")
public class Command_setlever extends FreedomCommand
{
    @Override
    public boolean run(CommandSender sender, Player playerSender, Command cmd, String commandLabel, String[] args, boolean senderIsConsole)
    {
        if (args.length != 5)
        {
            return false;
        }

        double x, y, z;
        try
        {
            x = Double.parseDouble(args[0]);
            y = Double.parseDouble(args[1]);
            z = Double.parseDouble(args[2]);

            // Shows the "Invalid coordinates" message without having to do boilerplate bullshit
            if (Math.abs(x) > 29999998 || Math.abs(y) > 29999998 || Math.abs(z) > 29999998)
            {
                throw new IllegalArgumentException();
            }
        }
        catch (IllegalArgumentException ex)
        {
            msg("Invalid coordinates.");
            return true;
        }

        Optional<World> optionalWorld = server.getWorlds().stream().filter(world ->
                world.getName().equalsIgnoreCase(args[3])).findAny();

        if (optionalWorld.isEmpty())
        {
            msg("Invalid world: " + args[3]);
            return true;
        }

        final Location leverLocation = new Location(optionalWorld.get(), x, y, z);
        final Block targetBlock = leverLocation.getBlock();

        if (targetBlock.getType() == Material.LEVER)
        {
            BlockState state = targetBlock.getState();
            BlockData data = state.getBlockData();
            Switch caster = (Switch) data;

            caster.setPowered(args[4].trim().equalsIgnoreCase("on") || args[4].trim().equalsIgnoreCase("1"));
            state.setBlockData(data);
            state.update();

            if (plugin.cpb.isEnabled())
            {
                plugin.cpb.getCoreProtectAPI().logInteraction(sender.getName(), leverLocation);
            }
        }
        else
        {
            msg("That block isn't a lever.");
        }

        return true;
    }
}
