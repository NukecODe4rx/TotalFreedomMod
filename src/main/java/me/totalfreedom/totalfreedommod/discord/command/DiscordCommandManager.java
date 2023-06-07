package me.totalfreedom.totalfreedommod.discord.command;

import me.totalfreedom.totalfreedommod.config.ConfigEntry;
import me.totalfreedom.totalfreedommod.discord.Discord;
import me.totalfreedom.totalfreedommod.util.FLog;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import org.reflections.Reflections;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

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

    public void parse(String content, Member member, TextChannel channel)
    {
        List<String> args = new ArrayList<>(Arrays.asList(content.split(" ")));

        final String alias = args.remove(0).split(PREFIX)[1]; // The joys of command parsing

        for (DiscordCommand command : commands)
        {
            if (command.getCommandName().equalsIgnoreCase(alias) || command.getAliases().contains(alias.toLowerCase()))
            {
                if (command.canExecute(member))
                {
                    final MessageCreateBuilder messageBuilder = command.execute(member, args);
                    final MessageCreateData message = messageBuilder.build();
                    final CompletableFuture<Message> futureMessage = channel.sendMessage(message).submit(true);

                    this.discord.sentMessages.add(futureMessage);
                }
                else
                {
                    final EmbedBuilder embedBuilder = new EmbedBuilder();
                    embedBuilder.setTitle("Command error");
                    embedBuilder.setColor(Color.RED);
                    embedBuilder.setDescription("You don't have permission to execute this command.");
                    final MessageEmbed embed = embedBuilder.build();

                    final CompletableFuture<Message> futureMessage = channel.sendMessage(MessageCreateData.fromEmbeds(embed)).submit(true);

                    this.discord.sentMessages.add(futureMessage);
                }
            }
        }
    }
}
