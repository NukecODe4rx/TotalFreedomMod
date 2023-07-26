package me.totalfreedom.totalfreedommod.discord;

import me.totalfreedom.totalfreedommod.config.ConfigEntry;
import me.totalfreedom.totalfreedommod.util.FLog;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.entities.emoji.EmojiUnion;
import net.dv8tion.jda.api.entities.emoji.UnicodeEmoji;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

public class MessageReactionListener extends ListenerAdapter
{
    public void onMessageReactionAdd(MessageReactionAddEvent messageReactionAddEvent)
    {
        if (!messageReactionAddEvent.isFromGuild())
        {
            return;
        }

        if (messageReactionAddEvent.getMember() == null)
        {
            return;
        }

        if (messageReactionAddEvent.getMember().getUser().getId().equals(Discord.bot.getSelfUser().getId()))
        {
            return;
        }

        if (!messageReactionAddEvent.getChannel().getId().equals(ConfigEntry.DISCORD_REPORT_CHANNEL_ID.getString()))
        {
            return;
        }

        EmojiUnion emojiUnion = messageReactionAddEvent.getEmoji();

        if (emojiUnion.getType() != Emoji.Type.UNICODE) {
            return;
        }

        UnicodeEmoji unicodeEmoji = emojiUnion.asUnicode();

        if (!unicodeEmoji.getAsReactionCode().equals("\uD83D\uDCCB"))
        {
            return;
        }

        final TextChannel archiveChannel = Discord.bot.getTextChannelById(ConfigEntry.DISCORD_REPORT_ARCHIVE_CHANNEL_ID.getString());

        if (archiveChannel == null)
        {
            FLog.warning("Report archive channel is defined in the config, yet doesn't actually exist!");
            return;
        }

        final Message message = messageReactionAddEvent.retrieveMessage().complete();
        final Member completer = messageReactionAddEvent.getMember();

        if (!message.getAuthor().getId().equals(Discord.bot.getSelfUser().getId()))
        {
            return;
        }

        // We don't need other embeds... yet?
        final MessageEmbed embed = message.getEmbeds().get(0);
        final MessageCreateBuilder createBuilder = MessageCreateBuilder.from(MessageCreateData.fromContent("Report completed by " + completer.getUser().getAsMention() + " (" + Discord.deformat(completer.getUser().getAsTag() + ")")));
        createBuilder.addEmbeds(embed);
        final MessageCreateData archiveMessage = createBuilder.build();

        archiveChannel.sendMessage(archiveMessage).complete();
        message.delete().complete();
    }
}
