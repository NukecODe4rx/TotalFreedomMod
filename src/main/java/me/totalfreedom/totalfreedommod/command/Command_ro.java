package me.totalfreedom.totalfreedommod.command;

import java.util.ArrayList;
import java.util.List;
import me.totalfreedom.totalfreedommod.rank.Rank;
import me.totalfreedom.totalfreedommod.util.FUtil;
import me.totalfreedom.totalfreedommod.util.Groups;
import net.coreprotect.CoreProtectAPI;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Waterlogged;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandPermissions(level = Rank.ADMIN, source = SourceType.BOTH)
@CommandParameters(description = "Remove all blocks of a certain type in the radius of certain players.", usage = "/<command> <block> [radius (default=50)] [player]")
public class Command_ro extends FreedomCommand
{

    private int removeBlocks(Location center, Material material, int radius, String user)
    {
        int affected = 0;

        Block centerBlock = center.getBlock();
        for (int xOffset = -radius; xOffset <= radius; xOffset++)
        {
            for (int yOffset = -radius; yOffset <= radius; yOffset++)
            {
                for (int zOffset = -radius; zOffset <= radius; zOffset++)
                {
                    Block block = centerBlock.getRelative(xOffset, yOffset, zOffset);
                    BlockData data = block.getBlockData();
                    CoreProtectAPI cpAPI = plugin.cpb.getCoreProtectAPI();

                    if (block.getLocation().distanceSquared(center) < (radius * radius))
                    {
                        if (material.equals(Material.WATER) && data instanceof Waterlogged waterloggedData)
                        {
                            if (cpAPI != null)
                            {
                                cpAPI.logRemoval(user, block.getLocation(), material, data);
                            }

                            if (waterloggedData.isWaterlogged())
                            {
                                waterloggedData.setWaterlogged(false);
                                block.setBlockData(waterloggedData);
                                affected++;
                                continue;
                            }

                            block.setType(Material.AIR);
                            affected++;
                        }
                        else if (block.getType().equals(material))
                        {
                            if (cpAPI != null)
                            {
                                cpAPI.logRemoval(user, block.getLocation(), material, data);
                            }

                            block.setType(Material.AIR);
                            affected++;
                        }
                    }
                }
            }
        }
        return affected;
    }

    @Override
    public boolean run(CommandSender sender, Player playerSender, Command cmd, String commandLabel, String[] args, boolean senderIsConsole)
    {
        if (args.length < 1 || args.length > 3)
        {
            return false;
        }

        final List<Material> materials = new ArrayList<>();
        String names = null;
        if (args[0].equalsIgnoreCase("shulker_boxes") || args[0].equalsIgnoreCase("shulkers"))
        {
            materials.addAll(Groups.SHULKER_BOXES);
            names = "shulker boxes";
        }
        else if (args[0].equalsIgnoreCase("banners") || args[0].equalsIgnoreCase("banner"))
        {
            materials.addAll(Groups.BANNERS);
            names = "banners";
        }
        else
        {
            for (String materialName : StringUtils.split(args[0], ","))
            {
                Material fromMaterial = Material.matchMaterial(materialName);

                if (fromMaterial == null || fromMaterial == Material.AIR || !fromMaterial.isBlock())
                {
                    msg("Invalid material: " + materialName, ChatColor.RED);
                    return true;
                }

                materials.add(fromMaterial);
            }
        }

        int radius = 50;
        if (args.length >= 2)
        {
            try
            {
                radius = Math.max(1, Math.min(50, Integer.parseInt(args[1])));
            }
            catch (NumberFormatException ex)
            {
                msg("Invalid radius: " + args[1], ChatColor.RED);
                return true;
            }
        }

        final Player targetPlayer;
        if (args.length == 3)
        {
            targetPlayer = getPlayer(args[2]);
            if (targetPlayer == null)
            {
                msg(FreedomCommand.PLAYER_NOT_FOUND);
                return true;
            }
        }
        else
        {
            targetPlayer = null;
        }

        if (names == null)
        {
            names = StringUtils.join(materials, ", ");
        }

        World adminWorld = null;
        try
        {
            adminWorld = plugin.wm.adminworld.getWorld();
        }
        catch (Exception ignored)
        {
        }

        int affected = 0;
        if (targetPlayer == null)
        {
            FUtil.adminAction(sender.getName(), "Removing all " + names + " within " + radius + " blocks of all players... Brace for lag!", false);

            for (final Player player : server.getOnlinePlayers())
            {
                if (player.getWorld() == adminWorld)
                {
                    continue;
                }

                for (final Material material : materials)
                {
                    affected += removeBlocks(player.getLocation(), material, radius, sender.getName());
                }
            }
        }
        else
        {
            if (targetPlayer.getWorld() != adminWorld)
            {
                FUtil.adminAction(sender.getName(), "Removing all " + names + " within " + radius + " blocks of " + targetPlayer.getName(), false);
                for (Material material : materials)
                {
                    affected += removeBlocks(targetPlayer.getLocation(), material, radius, sender.getName());
                }
            }
        }

        FUtil.adminAction(sender.getName(), "Remove complete! " + affected + " blocks removed.", false);
        return true;
    }
}