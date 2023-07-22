package me.totalfreedom.totalfreedommod.discord.commands;

import me.totalfreedom.totalfreedommod.discord.Discord;
import me.totalfreedom.totalfreedommod.discord.command.DiscordCommandImpl;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

import java.util.Collections;
import java.util.List;

public class PluralKitCommand extends DiscordCommandImpl
{
    @Override
    public String getCommandName()
    {
        return "pluralkit";
    }

    @Override
    public String getDescription()
    {
        return "Manage your PluralKit integration status. Arguments: <enable|disable|refresh>";
    }

    @Override
    public String getCategory()
    {
        return "PluralKit";
    }

    @Override
    public List<String> getAliases()
    {
        return Collections.singletonList("pk");
    }

    @Override
    public boolean isAdmin()
    {
        return false;
    }

    @Override
    public MessageCreateBuilder execute(Member member, List<String> args)
    {
        if (Discord.pluralKit == null) {
            return MessageCreateBuilder.from(MessageCreateData.fromContent("The PluralKit integration is globally disabled."));
        }

        if (args.isEmpty()) {
            return MessageCreateBuilder.from(MessageCreateData.fromContent("Usage: <enable|disable|refresh>"));
        }

        final String id = member.getId();
        switch (args.get(0)) {
            case "enable" -> {
                if (Discord.pluralKit.isEnabled(id)) {
                    return MessageCreateBuilder.from(MessageCreateData.fromContent("You have already enabled the PluralKit integration."));
                }

                Discord.pluralKit.setEnabled(id, true);

                try {
                    Discord.pluralKit.refresh(id);
                } catch (Exception e) {
                    e.printStackTrace();
                    Discord.pluralKit.setNullSystem(id);
                    return MessageCreateBuilder.from(MessageCreateData.fromContent("Failed to refresh your PluralKit data. Make sure your member list is public in the privacy settings."));

                }
                return MessageCreateBuilder.from(MessageCreateData.fromContent("Okay, PluralKit integration enabled. You can now use the refresh subcommand to sync your PluralKit data if it goes out-of-sync."));
            }
            case "disable" -> {
                if (!Discord.pluralKit.isEnabled(id)) {
                    return MessageCreateBuilder.from(MessageCreateData.fromContent("You have not enabled the PluralKit integration."));
                }

                Discord.pluralKit.setEnabled(id, false);
                return MessageCreateBuilder.from(MessageCreateData.fromContent("Okay, PluralKit integration disabled."));
            }
            case "refresh" -> {
                if (!Discord.pluralKit.isEnabled(id)) {
                    return MessageCreateBuilder.from(MessageCreateData.fromContent("You have not enabled the PluralKit integration."));
                }

                try {
                    Discord.pluralKit.refresh(id);
                } catch (Exception e) {
                    e.printStackTrace();
                    Discord.pluralKit.setNullSystem(id);
                    return MessageCreateBuilder.from(MessageCreateData.fromContent("Failed to refresh your PluralKit data. Make sure your member list is public in the privacy settings."));
                }
                return MessageCreateBuilder.from(MessageCreateData.fromContent("Okay, PluralKit data refreshed."));
            }
            default -> {
                return MessageCreateBuilder.from(MessageCreateData.fromContent("Unknown subcommand."));
            }
        }
    }
}
