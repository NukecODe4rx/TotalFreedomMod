package me.totalfreedom.totalfreedommod.command;

import java.util.*;
import me.totalfreedom.totalfreedommod.punishments.Punishment;
import me.totalfreedom.totalfreedommod.punishments.PunishmentType;
import me.totalfreedom.totalfreedommod.rank.Rank;
import me.totalfreedom.totalfreedommod.util.FUtil;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandPermissions(level = Rank.ADMIN, source = SourceType.BOTH)
@CommandParameters(description = "Mutes a player with brute force.", usage = "/<command> <[-s | -q] <player> [reason] | list | purge | all>", aliases = "stfu")
public class Command_mute extends FreedomCommand
{
    @Override
    public boolean run(CommandSender sender, Player playerSender, Command cmd, String commandLabel, String[] args, boolean senderIsConsole)
    {
        if (args.length == 0)
        {
            return false;
        }

        switch (args[0].toLowerCase())
        {
            case "list" ->
            {
                msg("Muted players:");
                List<? extends Player> list = server.getOnlinePlayers().stream().filter(player ->
                        plugin.pl.getPlayer(player).isMuted()).toList();

                if (list.size() > 0)
                    list.forEach(player -> msg("- " + player.getName()));
                else
                    msg("- none");
            }
            case "purge" ->
            {
                FUtil.adminAction(sender.getName(), "Unmuting all players", true);
                List<? extends Player> list = server.getOnlinePlayers().stream().filter(player ->
                        plugin.pl.getPlayer(player).isMuted()).toList();

                list.forEach(player ->
                {
                    plugin.pl.getPlayer(player).setMuted(false);
                    player.sendTitle(ChatColor.RED + "You have been unmuted.",
                            ChatColor.YELLOW + "Be sure to follow the rules!", 20, 100, 60);
                });

                msg("Unmuted " + list.size() + " player" + (list.size() != 1 ? "s" : "") + ".");
            }
            case "all" ->
            {
                FUtil.adminAction(sender.getName(), "Muting all non-admins", true);
                List<? extends Player> list = server.getOnlinePlayers().stream().filter(player ->
                        !plugin.al.isAdmin(player)).toList();

                list.forEach(player ->
                {
                    plugin.pl.getPlayer(player).setMuted(true);
                    player.sendTitle(ChatColor.RED + "You've been muted globally.",
                            ChatColor.YELLOW + "Please be patient and you will be unmuted shortly.", 20, 100, 60);
                });

                msg("Muted " + list.size() + " player" + (list.size() != 1 ? "s" : "") + ".");
            }
            default ->
            {
                boolean quiet = args[0].equalsIgnoreCase("-q");
                boolean smite = args[0].equalsIgnoreCase("-s");

                // Handling the -q parameter
                if (quiet || smite)
                {
                    if (args.length == 1) return false;
                    args = ArrayUtils.subarray(args, 1, args.length);
                }

                // Handling the (optional) reason
                String reason = args.length > 1 ? StringUtils.join(args, " ", 1, args.length) : null;

                // Showtime
                Optional.ofNullable(getPlayer(args[0])).ifPresentOrElse(player ->
                {
                    if (plugin.al.isAdmin(player))
                    {
                        msg(player.getName() + " is an admin, and as such can't be muted.", ChatColor.RED);
                        return;
                    }
                    else if (plugin.pl.getPlayer(player).isMuted())
                    {
                        msg(player.getName() + " is already muted.", ChatColor.RED);
                        return;
                    }

                    // Don't broadcast the mute if it was quiet
                    if (!quiet)
                    {
                        FUtil.adminAction(sender.getName(), "Muting " + player.getName(), true);
                    }

                    // Smite the player if we're supposed to
                    if (smite)
                    {
                        Command_smite.smite(sender, player, reason, true, false);
                    }

                    // Mutes the player
                    plugin.pl.getPlayer(player).setMuted(true);

                    // Notify the player that they have been muted
                    player.sendTitle(ChatColor.RED + "You've been muted.",
                            ChatColor.YELLOW + "Be sure to follow the rules!", 20, 100, 60);
                    msg(player, "You have been muted by " + ChatColor.YELLOW + sender.getName()
                            + ChatColor.RED + ".", ChatColor.RED);

                    // Give them the reason if one is present.
                    if (reason != null)
                    {
                        msg(player, "Reason: " + ChatColor.YELLOW + reason, ChatColor.RED);
                    }

                    msg((quiet ? "Quietly m" : "M") + "uted " + player.getName() + ".");
                    plugin.pul.logPunishment(new Punishment(player.getName(), FUtil.getIp(player), sender.getName(),
                            PunishmentType.MUTE, reason));
                }, () -> msg(PLAYER_NOT_FOUND));
            }
        }

        return true;
    }

    @Override
    public List<String> getTabCompleteOptions(CommandSender sender, Command command, String alias, String[] args)
    {
        if (!plugin.al.isAdmin(sender))
        {
            return null;
        }

        if (args.length == 1)
        {
            List<String> arguments = new ArrayList<>();
            arguments.addAll(FUtil.getPlayerList());
            arguments.addAll(Arrays.asList("list", "purge", "all"));
            return arguments;
        }

        return Collections.emptyList();
    }
}
