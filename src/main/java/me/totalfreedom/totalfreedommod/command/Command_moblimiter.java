package me.totalfreedom.totalfreedommod.command;

import me.totalfreedom.totalfreedommod.GameRuleHandler;
import me.totalfreedom.totalfreedommod.config.ConfigEntry;
import me.totalfreedom.totalfreedommod.rank.Rank;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandPermissions(level = Rank.ADMIN, source = SourceType.BOTH)
@CommandParameters(description = "Control mob limiting parameters.", usage = "/<command> <on | off | setmax <count> | dragon | giant | ghast | slime>")
public class Command_moblimiter extends FreedomCommand
{
    @Override
    public boolean run(CommandSender sender, Player playerSender, Command cmd, String commandLabel, String[] args, boolean senderIsConsole)
    {
        if (args.length < 1)
        {
            return false;
        }

        switch (args[0].toLowerCase())
        {
            case "on" -> ConfigEntry.MOB_LIMITER_ENABLED.setBoolean(true);
            case "off" -> ConfigEntry.MOB_LIMITER_ENABLED.setBoolean(false);
            case "dragon" -> ConfigEntry.MOB_LIMITER_DISABLE_DRAGON.setBoolean(!ConfigEntry.MOB_LIMITER_DISABLE_DRAGON.getBoolean());
            case "giant" -> ConfigEntry.MOB_LIMITER_DISABLE_GIANT.setBoolean(!ConfigEntry.MOB_LIMITER_DISABLE_GIANT.getBoolean());
            case "slime" -> ConfigEntry.MOB_LIMITER_DISABLE_SLIME.setBoolean(!ConfigEntry.MOB_LIMITER_DISABLE_SLIME.getBoolean());
            case "ghast" -> ConfigEntry.MOB_LIMITER_DISABLE_GHAST.setBoolean(!ConfigEntry.MOB_LIMITER_DISABLE_GHAST.getBoolean());
            case "setmax" ->
            {
                if (args.length < 2)
                {
                    return false;
                }

                try
                {
                    ConfigEntry.MOB_LIMITER_MAX.setInteger(Math.max(0, Math.min(2000, Integer.parseInt(args[1]))));
                }
                catch (Exception ex)
                {
                    msg("Invalid number: " + args[1], ChatColor.RED);
                    return true;
                }
            }
        }

        if (ConfigEntry.MOB_LIMITER_ENABLED.getBoolean())
        {
            msg("Moblimiter enabled. Maximum mobcount set to: " + ConfigEntry.MOB_LIMITER_MAX.getInteger() + ".");

            msg("Dragon: " + (ConfigEntry.MOB_LIMITER_DISABLE_DRAGON.getBoolean() ? "disabled" : "enabled") + ".");
            msg("Giant: " + (ConfigEntry.MOB_LIMITER_DISABLE_GIANT.getBoolean() ? "disabled" : "enabled") + ".");
            msg("Slime: " + (ConfigEntry.MOB_LIMITER_DISABLE_SLIME.getBoolean() ? "disabled" : "enabled") + ".");
            msg("Ghast: " + (ConfigEntry.MOB_LIMITER_DISABLE_GHAST.getBoolean() ? "disabled" : "enabled") + ".");
        }
        else
        {
            msg("Moblimiter is disabled. No mob restrictions are in effect.");
        }

        plugin.gr.setGameRule(GameRuleHandler.GameRule.DO_MOB_SPAWNING, !ConfigEntry.MOB_LIMITER_ENABLED.getBoolean());
        return true;
    }
}
