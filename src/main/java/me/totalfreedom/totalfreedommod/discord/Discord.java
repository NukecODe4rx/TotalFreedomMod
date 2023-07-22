package me.totalfreedom.totalfreedommod.discord;

import com.google.common.base.Strings;

import java.lang.reflect.Method;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import com.google.common.collect.ImmutableList;
import me.totalfreedom.totalfreedommod.FreedomService;
import me.totalfreedom.totalfreedommod.admin.Admin;
import me.totalfreedom.totalfreedommod.config.ConfigEntry;
import me.totalfreedom.totalfreedommod.discord.command.DiscordCommandManager;
import me.totalfreedom.totalfreedommod.discord.pluralkit.PluralKitIntegration;
import me.totalfreedom.totalfreedommod.player.PlayerData;
import me.totalfreedom.totalfreedommod.rank.Rank;
import me.totalfreedom.totalfreedommod.util.FLog;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.requests.restaction.AuditableRestAction;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.md_5.bungee.api.ChatColor;
import org.apache.commons.lang.WordUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.bukkit.GameRule;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

public class Discord extends FreedomService
{
    private static final ImmutableList<String> DISCORD_SUBDOMAINS = ImmutableList.of("discordapp.com", "discord.com", "discord.gg");
    private static final Pattern CHAT_COLOR_PATTERN = Pattern.compile("&([0-9a-fA-Fk-oK-OrRxX]|#[a-fA-F0-9]{6})");

    private final List<String> messageQueue = new ArrayList<>();
    private final List<String> adminChatMessageQueue = new ArrayList<>();
    private ScheduledExecutorService queueSenderExecutor;

    public static HashMap<String, PlayerData> LINK_CODES = new HashMap<>();
    public static JDA bot = null;
    public static PluralKitIntegration pluralKit = null;
    public static DiscordCommandManager DISCORD_COMMAND_MANAGER;
    public Boolean enabled = false;

    public static String getCode(PlayerData playerData)
    {
        for (String code : LINK_CODES.keySet())
        {
            if (LINK_CODES.get(code).equals(playerData))
            {
                return code;
            }
        }
        return null;
    }

    // I need these so the server doesn't throw a fit when JDA isn't present.
    private static void addRole(final Guild server, final Member member, final Role role) {
        try
        {
            final Method method = server.getClass().getMethod("addRoleToMember", UserSnowflake.class, Role.class);
            ((AuditableRestAction<Void>)method.invoke(server, member, role)).complete();
        }
        catch (Exception ignored) {}
    }

    private static void removeRole(final Guild server, final Member member, final Role role) {
        try
        {
            final Method method = server.getClass().getMethod("removeRoleFromMember", UserSnowflake.class, Role.class);
            ((AuditableRestAction<Void>)method.invoke(server, member, role)).complete();
        }
        catch (Exception ignored) {}
    }

    private static Object getEmoji(final String unicode) {
        try
        {
            final Method method = Emoji.class.getMethod("fromUnicode", String.class);
            return method.invoke(null, unicode);
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    public static boolean syncRoles(Admin admin, String discordID)
    {
        if (discordID == null)
        {
            return false;
        }

        Guild server = bot.getGuildById(ConfigEntry.DISCORD_SERVER_ID.getString());
        if (server == null)
        {
            FLog.severe("The Discord server ID specified is invalid, or the bot is not on the server.");
            return false;
        }

        Member member = server.getMemberById(discordID);
        if (member == null)
        {
            return false;
        }

        Role adminRole = server.getRoleById(ConfigEntry.DISCORD_NEW_ADMIN_ROLE_ID.getString());
        if (adminRole == null)
        {
            FLog.severe("The specified Admin role does not exist!");
            return false;
        }

        Role senioradminRole = server.getRoleById(ConfigEntry.DISCORD_SENIOR_ADMIN_ROLE_ID.getString());
        if (senioradminRole == null)
        {
            FLog.severe("The specified Senior Admin role does not exist!");
            return false;
        }

        if (!admin.isActive())
        {
            if (member.getRoles().contains(adminRole))
            {
                removeRole(server, member, adminRole);
            }
            if (member.getRoles().contains(senioradminRole))
            {
                removeRole(server, member, senioradminRole);
            }
            return true;
        }

        if (admin.getRank().equals(Rank.ADMIN))
        {
            if (!member.getRoles().contains(adminRole))
            {
                addRole(server, member, adminRole);
            }
            if (member.getRoles().contains(senioradminRole))
            {
                removeRole(server, member, senioradminRole);
            }
            return true;
        }
        else if (admin.getRank().equals(Rank.SENIOR_ADMIN))
        {
            if (!member.getRoles().contains(senioradminRole))
            {
                addRole(server, member, senioradminRole);
            }
            if (member.getRoles().contains(adminRole))
            {
                removeRole(server, member, adminRole);
            }
            return true;
        }
        return false;
    }

    public void startBot()
    {
        if (queueSenderExecutor != null) {
            queueSenderExecutor.shutdown();
        }

        queueSenderExecutor = Executors.newSingleThreadScheduledExecutor();
        messageQueue.clear();
        adminChatMessageQueue.clear();

        DISCORD_COMMAND_MANAGER = new DiscordCommandManager();
        DISCORD_COMMAND_MANAGER.init(this);

        enabled = !Strings.isNullOrEmpty(ConfigEntry.DISCORD_TOKEN.getString());
        if (!enabled)
        {
            return;
        }

        if (bot != null)
        {
            for (Object object : bot.getRegisteredListeners())
            {
                bot.removeEventListener(object);
            }
        }

        try
        {
            bot = JDABuilder.createDefault(ConfigEntry.DISCORD_TOKEN.getString())
                    .addEventListeners(new PrivateMessageListener(),
                            new DiscordToMinecraftListener(),
                            new DiscordToAdminChatListener(),
                            new MessageReactionListener(),
                            new ListenerAdapter()
                            {
                                @Override
                                public void onReady(@NotNull ReadyEvent event)
                                {
                                    new StartEvent().start();
                                }
                            })
                    .setAutoReconnect(true)
                    .setChunkingFilter(ChunkingFilter.ALL)
                    .setMemberCachePolicy(MemberCachePolicy.ALL)
                    .enableIntents(GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_MESSAGES, GatewayIntent.MESSAGE_CONTENT)
                    .build();
            FLog.info("Discord integration has successfully enabled!");
        }
        catch (IllegalArgumentException e)
        {
            FLog.warning("Discord integration failed to start.");
            enabled = false;
        }
        catch (NoClassDefFoundError e)
        {
            FLog.warning("The JDA plugin is not installed, therefore the discord bot cannot start.");
            FLog.warning("To resolve this error, please download the latest JDA from: https://github.com/AtlasMediaGroup/Minecraft-JDA/releases");
            enabled = false;
        }

        if (ConfigEntry.DISCORD_PLURALKIT_INTEGRATION.getBoolean()) {
            pluralKit = new PluralKitIntegration();
        }

        final String chatChannelId = ConfigEntry.DISCORD_CHAT_CHANNEL_ID.getString();
        final String adminChatChannelId = ConfigEntry.DISCORD_ADMINCHAT_CHANNEL_ID.getString();
        if (chatChannelId != null || adminChatChannelId != null)
        {
            queueSenderExecutor.scheduleAtFixedRate(() -> {
                final TextChannel chatChannel = !Strings.isNullOrEmpty(chatChannelId) ? bot.getTextChannelById(chatChannelId) : null;
                final TextChannel adminChatChannel = !Strings.isNullOrEmpty(adminChatChannelId) ? bot.getTextChannelById(adminChatChannelId) : null;

                final String chatChannelMessage = String.join("\n", messageQueue);
                final String adminChatChannelMessage = String.join("\n", adminChatMessageQueue);
                messageQueue.clear();
                adminChatMessageQueue.clear();

                if (chatChannel != null && !chatChannelMessage.isEmpty()) {
                    chatChannel.sendMessage(chatChannelMessage).submit(true);
                }
                if (adminChatChannel != null && !adminChatChannelMessage.isEmpty()) {
                    adminChatChannel.sendMessage(adminChatChannelMessage).submit(true);
                }
            }, 1, 1, TimeUnit.SECONDS);
        }

    }

    public void clearQueue()
    {
        messageQueue.clear();
        adminChatMessageQueue.clear();
        messageChatChannel("**Message queue cleared**", true);
    }

    public String generateCode(int size)
    {
        return RandomStringUtils.randomNumeric(size);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerDeath(PlayerDeathEvent event)
    {
        //Avoiding NPE Unboxing Warnings
        Boolean b = event.getEntity().getWorld().getGameRuleValue(GameRule.SHOW_DEATH_MESSAGES);
        if (b == null || !b)
        {
            return;
        }

        Component deathMessage = event.deathMessage();

        if (deathMessage != null)
        {
            messageChatChannel("**" + PlainTextComponentSerializer.plainText().serialize(deathMessage) + "**", true);
        }
    }

    @Override
    public void onStart()
    {
        startBot();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event)
    {
        if (!plugin.al.isVanished(event.getPlayer().getUniqueId()))
        {
            messageChatChannel("**" + event.getPlayer().getName() + " joined the server" + "**", true);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerLeave(PlayerQuitEvent event)
    {
        if (!plugin.al.isVanished(event.getPlayer().getUniqueId()))
        {
            messageChatChannel("**" + event.getPlayer().getName() + " left the server" + "**", true);
        }
    }

    public static String sanitizeChatMessage(String message)
    {
        String newMessage = message;

        if (message.contains("@"))
        {
            // \u200B is Zero Width Space, invisible on Discord
            newMessage = newMessage.replaceAll("@", "@\u200B");
        }

        if (message.toLowerCase().contains("discord.gg")) // discord.gg/invite works as an invite
        {
            return "";
        }

        for (String subdomain : DISCORD_SUBDOMAINS)
        {
            if (message.toLowerCase().contains(subdomain + "/invite"))
            {
                return "";
            }
        }

        if (message.contains("§"))
        {
            newMessage = newMessage.replaceAll("§", "");
        }

        newMessage = CHAT_COLOR_PATTERN.matcher(newMessage).replaceAll("");

        return deformat(newMessage);
    }

    public void messageChatChannel(String message, boolean system)
    {
        String sanitizedMessage = system ? message : sanitizeChatMessage(message);
        if (sanitizedMessage.isBlank()) return;

        if (enabled)
        {
            if (messageQueue.size() >= 5) {
                messageQueue.remove(0);
            }

            messageQueue.add(sanitizedMessage);
        }
    }

    public void messageAdminChatChannel(String message, boolean system)
    {
        String sanitizedMessage = sanitizeChatMessage(message);
        if (sanitizedMessage.isBlank()) return;

        if (enabled)
        {
            if (adminChatMessageQueue.size() >= 5) {
                adminChatMessageQueue.remove(0);
            }

            adminChatMessageQueue.add(sanitizedMessage);
        }
    }

    public String formatBotTag()
    {
        SelfUser user = bot.getSelfUser();
        return user.getAsTag();
    }

    @Override
    public void onStop()
    {
        if (bot != null)
        {
            messageChatChannel("**Server has stopped**", true);
        }

        FLog.info("Discord integration has successfully shutdown.");
    }

    public static String deformat(String input)
    {
        return input.replaceAll("([_\\\\`*>|])", "\\\\$1");
    }

    public boolean shouldISendReport()
    {
        if (ConfigEntry.DISCORD_REPORT_CHANNEL_ID.getString().isEmpty())
        {
            return false;
        }

        if (ConfigEntry.DISCORD_SERVER_ID.getString().isEmpty())
        {
            FLog.severe("No Discord server ID was specified in the config, but there is a report channel ID.");
            return false;
        }

        Guild server = bot.getGuildById(ConfigEntry.DISCORD_SERVER_ID.getString());
        if (server == null)

        {
            FLog.severe("The Discord server ID specified is invalid, or the bot is not on the server.");
            return false;
        }

        TextChannel channel = server.getTextChannelById(ConfigEntry.DISCORD_REPORT_CHANNEL_ID.getString());
        if (channel == null)
        {
            FLog.severe("The report channel ID specified in the config is invalid.");
            return false;
        }

        return true;
    }

    public boolean sendReportOffline(Player reporter, OfflinePlayer reported, String reason)
    {
        if (!shouldISendReport())
        {
            return false;
        }

        final Guild server = bot.getGuildById(ConfigEntry.DISCORD_SERVER_ID.getString());
        final TextChannel channel = server.getTextChannelById(ConfigEntry.DISCORD_REPORT_CHANNEL_ID.getString());

        final EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle("Report for " + reported.getName() + " (offline)");
        embedBuilder.setDescription(reason);
        embedBuilder.setFooter("Reported by " + reporter.getName(), "https://minotar.net/helm/" + reporter.getName() + ".png");
        embedBuilder.setTimestamp(Instant.from(ZonedDateTime.now()));
        if (plugin.esb.isEnabled())
        {
            com.earth2me.essentials.User user = plugin.esb.getEssentialsUser(reported.getName());
            String location = "World: " + Objects.requireNonNull(user.getLastLocation().getWorld()).getName() + ", X: " + user.getLastLocation().getBlockX() + ", Y: " + user.getLastLocation().getBlockY() + ", Z: " + user.getLastLocation().getBlockZ();
            embedBuilder.addField("Location", location, true);
            embedBuilder.addField("God Mode", WordUtils.capitalizeFully(String.valueOf(user.isGodModeEnabled())), true);
            if (user.getNickname() != null)
            {
                embedBuilder.addField("Nickname", user.getNickname(), true);
            }
        }
        MessageEmbed embed = embedBuilder.build();
        Message message = channel.sendMessage(MessageCreateData.fromEmbeds(embed)).complete();

        if (!ConfigEntry.DISCORD_REPORT_ARCHIVE_CHANNEL_ID.getString().isEmpty())
        {
            message.addReaction((Emoji)getEmoji("\uD83D\uDCCB")).complete();
        }

        return true;
    }

    public boolean sendReport(Player reporter, Player reported, String reason)
    {
        if (!shouldISendReport())
        {
            return false;
        }

        final Guild server = bot.getGuildById(ConfigEntry.DISCORD_SERVER_ID.getString());
        final TextChannel channel = server.getTextChannelById(ConfigEntry.DISCORD_REPORT_CHANNEL_ID.getString());

        final EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle("Report for " + reported.getName());
        embedBuilder.setDescription(reason);
        embedBuilder.setFooter("Reported by " + reporter.getName(), "https://minotar.net/helm/" + reporter.getName() + ".png");
        embedBuilder.setTimestamp(Instant.from(ZonedDateTime.now()));
        String location = "World: " + Objects.requireNonNull(reported.getLocation().getWorld()).getName() + ", X: " + reported.getLocation().getBlockX() + ", Y: " + reported.getLocation().getBlockY() + ", Z: " + reported.getLocation().getBlockZ();
        embedBuilder.addField("Location", location, true);
        embedBuilder.addField("Game Mode", WordUtils.capitalizeFully(reported.getGameMode().name()), true);

        if (plugin.esb.isEnabled())
        {
            com.earth2me.essentials.User user = plugin.esb.getEssentialsUser(reported.getName());
            embedBuilder.addField("God Mode", WordUtils.capitalizeFully(String.valueOf(user.isGodModeEnabled())), true);
            if (user.getNickname() != null)
            {
                embedBuilder.addField("Nickname", user.getNickname(), true);
            }
        }

        MessageEmbed embed = embedBuilder.build();
        Message message = channel.sendMessage(MessageCreateData.fromEmbeds(embed)).complete();

        if (!ConfigEntry.DISCORD_REPORT_ARCHIVE_CHANNEL_ID.getString().isEmpty())
        {
            message.addReaction((Emoji)getEmoji("\uD83D\uDCCB")).complete();
        }

        return true;
    }

    // Do no ask why this is here. I spent two hours trying to make a simple thing work
    public class StartEvent
    {
        public void start()
        {
            messageChatChannel("**Server has started**", true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onAsyncPlayerChat(AsyncPlayerChatEvent event)
    {
        Player player = event.getPlayer();
        String message = event.getMessage();

        if (!server.hasWhitelist()
                && !plugin.pl.getPlayer(player).isMuted() && bot != null)
        {
            messageChatChannel(player.getName() + " \u00BB " + ChatColor.stripColor(message), false);
        }
    }
}
