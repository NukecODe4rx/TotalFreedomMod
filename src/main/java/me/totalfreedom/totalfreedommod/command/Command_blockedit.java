package me.totalfreedom.totalfreedommod.command;

import me.totalfreedom.totalfreedommod.player.FPlayer;
import me.totalfreedom.totalfreedommod.punishments.Punishment;
import me.totalfreedom.totalfreedommod.punishments.PunishmentType;
import me.totalfreedom.totalfreedommod.rank.Rank;
import me.totalfreedom.totalfreedommod.util.FUtil;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Optional;

@CommandPermissions(level = Rank.ADMIN, source = SourceType.BOTH)
@CommandParameters(description = "Restricts/restores block modification abilities for everyone on the server or a certain player.", usage = "/<command> [[-s] <player> [reason] | list | purge | all]")
public class Command_blockedit extends FreedomCommand
{
    @Override
    public boolean run(final CommandSender sender, final Player playerSender, final Command cmd, final String commandLabel, String[] args, final boolean senderIsConsole)
    {
        if (args.length > 0)
        {
            switch (args[0].toLowerCase())
            {
                case "list" ->
                {
                    List<? extends Player> list = server.getOnlinePlayers().stream().filter(player ->
                            plugin.pl.getPlayer(player).isEditBlocked()).sorted().toList();

                    // Oh dear god, why do I have to do it like this?
                    msg("There " + (list.size() != 1 ? "are " : "is ") + list.size() + " player"
                            + (list.size() != 1 ? "s" : "") + " online with restricted block modification abilities"
                            + (list.size() > 0 ? ":" : "."));

                    list.forEach(player -> msg("- " + player.getName()));
                }
                case "purge" ->
                {
                    FUtil.adminAction(sender.getName(), "Restoring block modification abilities for all players", true);

                    List<? extends Player> list = server.getOnlinePlayers().stream().filter(player ->
                            plugin.pl.getPlayer(player).isEditBlocked()).toList();

                    list.forEach(player ->
                    {
                        plugin.pl.getPlayer(player).setEditBlocked(false);
                        msg(player, "Your block modification abilities have been restored.", ChatColor.GREEN);
                    });

                    msg("Restored block modification abilities for " + list.size() + " player"
                            + (list.size() != 1 ? "s" : "") + ".");
                }
                case "all", "-a" ->
                {
                    FUtil.adminAction(sender.getName(), "Restricting block modification abilities for all non-admins", true);

                    List<? extends Player> list = server.getOnlinePlayers().stream().filter(player ->
                            !plugin.al.isAdmin(player)).toList();

                    list.forEach(player ->
                    {
                        plugin.pl.getPlayer(player).setEditBlocked(true);
                        msg(player, "Your block modification abilities have been restricted.", ChatColor.RED);
                    });

                    msg("Restricted block modification abilities for " + list.size() + " player"
                            + (list.size() != 1 ? "s" : "") + ".");
                }
                default -> Optional.ofNullable(getPlayer(args[0])).ifPresentOrElse(player ->
                {
                    FPlayer fPlayer = plugin.pl.getPlayer(player);

                    if (fPlayer.isEditBlocked())
                    {
                        FUtil.adminAction(sender.getName(), "Restoring block modification abilities for " + player.getName(), true);
                        fPlayer.setEditBlocked(false);
                        msg("Restored block modification abilities for " + player.getName() + ".");
                        msg(player, "Your block modification abilities have been restored.", ChatColor.GREEN);
                    }
                    else
                    {
                        if (plugin.al.isAdmin(player))
                        {
                            msg(player.getName() + " is an admin, and as such cannot have their block modification abilities restricted.", ChatColor.RED);
                        }
                        else
                        {
                            FUtil.adminAction(sender.getName(), "Restricting block modification abilities for " + player.getName(), true);
                            fPlayer.setEditBlocked(true);
                            msg("Restricted block modification abilities for " + player.getName() + ".");
                            msg(player, "Your block modification abilities have been restricted.", ChatColor.RED);

                            plugin.pul.logPunishment(new Punishment(player.getName(), FUtil.getIp(player),
                                    sender.getName(), PunishmentType.BLOCKEDIT, null));
                        }
                    }

                }, () -> msg(PLAYER_NOT_FOUND));
            }

            return true;
        }
        else
        {
            return false;
        }
    }
}