package me.totalfreedom.totalfreedommod.command;

import java.util.*;
import me.totalfreedom.totalfreedommod.rank.Rank;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

@CommandPermissions(level = Rank.ADMIN, source = SourceType.BOTH)
@CommandParameters(description = "Enable, disable, or reload a specified plugin, as well as list all plugins on the server.", usage = "/<command> <<enable | disable | reload> <pluginname>> | list>", aliases = "plc")
public class Command_plugincontrol extends FreedomCommand
{
    private final List<String> UNTOUCHABLE_PLUGINS = Arrays.asList(plugin.getName());

    @Override
    public boolean run(CommandSender sender, Player playerSender, Command cmd, String commandLabel, String[] args, boolean senderIsConsole)
    {
        final PluginManager pm = server.getPluginManager();

        /* This is the way it is because there was too much "if the arguments aren't enough then return false" in the
        *   original code in addition to the stupid amount of "if something isn't right then do some boilerplate stuff
        *   then return true". Codacy complained, so I aggressively optimized this to keep it quiet. */
        switch (args.length)
        {
            case 1 ->
            {
                if (args[0].equalsIgnoreCase("list"))
                {
                    Arrays.stream(pm.getPlugins()).forEach(pl ->
                    {
                        final String version = pl.getDescription().getVersion();
                        msg(ChatColor.GRAY + "- " + (pl.isEnabled() ? ChatColor.GREEN : ChatColor.RED) + pl.getName()
                                + ChatColor.GOLD + (!version.isEmpty() ? " v" + version : "") + " by "
                                + StringUtils.join(pl.getDescription().getAuthors(), ", "));
                    });

                    return true;
                }
            }
            case 2 ->
            {
                Plugin pl = pm.getPlugin(args[1]);

                if (pl != null)
                {
                    switch (args[0].toLowerCase())
                    {
                        case "enable" ->
                        {
                            if (pl.isEnabled())
                            {
                                msg(pl.getName() + " is already enabled.");
                                return true;
                            }

                            pm.enablePlugin(pl);

                            if (pl.isEnabled())
                            {
                                msg(pl.getName() + " is now enabled.");
                            }
                            else
                            {
                                msg("An error occurred whilst attempting to enable " + pl.getName() + ".");
                            }
                            return true;
                        }
                        case "disable" ->
                        {
                            if (!pl.isEnabled())
                            {
                                msg(pl.getName() + " is already disabled.");
                                return true;
                            }
                            else if (UNTOUCHABLE_PLUGINS.contains(pl.getName()))
                            {
                                msg(pl.getName() + " can't be disabled.");
                                return true;
                            }

                            pm.disablePlugin(pl);

                            msg(pl.getName() + " is now disabled.");
                            return true;
                        }
                        case "reload" ->
                        {
                            if (UNTOUCHABLE_PLUGINS.contains(pl.getName()))
                            {
                                msg(pl.getName() + " can't be reloaded.");
                                return true;
                            }

                            pm.disablePlugin(pl);
                            pm.enablePlugin(pl);

                            msg(pl.getName() + " has been reloaded.");
                            return true;
                        }
                        default ->
                        {
                            // Do nothing. This is here to please Codacy.
                        }
                    }
                }
                else
                {
                    msg("Plugin not found!");
                    return true;
                }
            }
            default ->
            {
                // Ditto
            }
        }

        return false;
    }

    @Override
    public List<String> getTabCompleteOptions(CommandSender sender, Command command, String alias, String[] args)
    {
        if (!plugin.al.isAdmin(sender))
        {
            return Collections.emptyList();
        }
        if (args.length == 1)
        {
            return Arrays.asList("enable", "disable", "reload", "list");
        }
        else if (args.length == 2 && !args[0].equalsIgnoreCase("list"))
        {
            return Arrays.stream(server.getPluginManager().getPlugins()).map(Plugin::getName)
                    .filter(pl -> !UNTOUCHABLE_PLUGINS.contains(pl)).toList();
        }

        return Collections.emptyList();
    }
}