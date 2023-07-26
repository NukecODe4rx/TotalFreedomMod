package me.totalfreedom.totalfreedommod.discord.command;

import me.totalfreedom.totalfreedommod.config.ConfigEntry;
import me.totalfreedom.totalfreedommod.discord.Discord;
import me.totalfreedom.totalfreedommod.util.FLog;
import me.totalfreedom.totalfreedommod.util.Reflections;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class DiscordCommandManager
{
    public static final String PREFIX = ConfigEntry.DISCORD_PREFIX.getString();
    private Discord discord;
    public final List<DiscordCommand> commands = new ArrayList<>();

    public void init(Discord discord)
    {
        this.discord = discord;

        final Reflections discordCommandsDir = new Reflections("me.totalfreedom.totalfreedommod.discord.commands");

        final Set<Class<? extends DiscordCommand>> commandClasses = discordCommandsDir.getSubTypesOf(DiscordCommand.class);

        for (Class<? extends DiscordCommand> commandClass : commandClasses)
        {
            try
            {
                commands.add(commandClass.getDeclaredConstructor().newInstance());
            }
            catch (Exception e)
            {
                FLog.warning("Failed to load Discord command: " + commandClass.getName());
            }
        }

        FLog.info("Loaded " + commands.size() + " Discord commands.");
    }

    public boolean parse(String content, Member member, TextChannel channel)
    {
        final String actualContent = content.substring(PREFIX.length()).trim();
        List<String> args = new ArrayList<>(Arrays.asList(actualContent.split(" ")));
        if (args.isEmpty())
        {
            return false;
        }

        final String alias = args.get(0);

        if (alias.isEmpty())
        {
            return false;
        }

        for (DiscordCommand command : commands)
        {
            if (command.getCommandName().equalsIgnoreCase(alias) || command.getAliases().contains(alias.toLowerCase()))
            {
                if (command.canExecute(member))
                {
                    final MessageCreateBuilder messageBuilder = command.execute(member, args);
                    final MessageCreateData message = messageBuilder.build();
                    channel.sendMessage(message).submit(true);
                }
                else
                {
                    final EmbedBuilder embedBuilder = new EmbedBuilder();
                    embedBuilder.setTitle("Command error");
                    embedBuilder.setColor(Color.RED);
                    embedBuilder.setDescription("You don't have permission to execute this command.");
                    final MessageEmbed embed = embedBuilder.build();
                    channel.sendMessage(MessageCreateData.fromEmbeds(embed)).submit(true);
                }
                return true;
            }
        }

        return false;
    }
}
