package me.totalfreedom.totalfreedommod.banning;

import com.google.common.collect.Lists;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import me.totalfreedom.totalfreedommod.config.ConfigEntry;
import me.totalfreedom.totalfreedommod.util.FUtil;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Ban
{

    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm:ss z");
    private final List<String> ips = Lists.newArrayList();
    private String username = null;
    private UUID uuid = null;
    private String by = null;


    private Date at = null;


    private String reason = null; // Unformatted, &[0-9,a-f] instead of ChatColor


    private long expiryUnix = -1;

    public Ban()
    {
    }

    public Ban(String username, UUID uuid, String ip, String by, Date at, Date expire, String reason)
    {
        this(username,
                uuid,
                Collections.singletonList(ip),
                by,
                at,
                expire,
                reason);
    }

    public Ban(String username, UUID uuid, List<String> ips, String by, Date at, Date expire, String reason)
    {
        this.username = username;
        this.uuid = uuid;
        if (ips != null)
        {
            this.ips.addAll(ips);
        }
        dedupeIps();
        this.by = by;
        this.at = at;
        if (expire == null)
        {
            expire = FUtil.parseDateOffset("24h");
        }
        this.expiryUnix = FUtil.getUnixTime(expire);
        this.reason = reason;
    }

    //
    // For player IP
    public static Ban forPlayerIp(Player player, CommandSender by)
    {
        return forPlayerIp(player, by, null, null);
    }

    public static Ban forPlayerIp(Player player, CommandSender by, Date expiry, String reason)
    {
        return new Ban(null, null, Collections.singletonList(FUtil.getIp(player)), by.getName(), Date.from(Instant.now()), expiry, reason);
    }

    public static Ban forPlayerIp(String ip, CommandSender by, Date expiry, String reason)
    {
        return new Ban(null, null, ip, by.getName(), Date.from(Instant.now()), expiry, reason);
    }

    //
    // For player name
    public static Ban forPlayerName(Player player, CommandSender by, Date expiry, String reason)
    {
        return forPlayerName(player.getName(), by, expiry, reason);
    }

    public static Ban forPlayerName(String player, CommandSender by, Date expiry, String reason)
    {
        return new Ban(player,
                null,
                new ArrayList<>(),
                by.getName(),
                Date.from(Instant.now()),
                expiry,
                reason);
    }

    //
    // For player
    public static Ban forPlayer(Player player, CommandSender by)
    {
        return forPlayerName(player, by, null, null);
    }

    public static Ban forPlayer(Player player, CommandSender by, Date expiry, String reason)
    {
        return new Ban(player.getName(),
                player.getUniqueId(),
                FUtil.getIp(player),
                by.getName(),
                Date.from(Instant.now()),
                expiry,
                reason);
    }

    public static SimpleDateFormat getDateFormat()
    {
        return DATE_FORMAT;
    }

    public boolean hasUsername()
    {
        return username != null && !username.isEmpty();
    }

    public boolean hasUUID()
    {
        return uuid != null;
    }

    public boolean addIp(String ip)
    {
        return ips.add(ip);
    }

    public boolean removeIp(String ip)
    {
        return ips.remove(ip);
    }

    public boolean hasIps()
    {
        return !ips.isEmpty();
    }

    public boolean hasExpiry()
    {
        return expiryUnix > 0;
    }

    public boolean isExpired()
    {
        return hasExpiry() && FUtil.getUnixDate(expiryUnix).before(new Date(FUtil.getUnixTime()));
    }

    public String bakeKickMessage()
    {
        final StringBuilder message = new StringBuilder(ChatColor.GOLD + "You");

        if (!hasUsername())
        {
            message.append("r IP address is");
        }
        else if (!hasIps())
        {
            message.append("r username is");
        }
        else
        {
            message.append(" are");
        }

        message.append(" temporarily banned from this server.");
        message.append("\nAppeal at ").append(ChatColor.BLUE)
                .append(ConfigEntry.SERVER_BAN_URL.getString());

        if (reason != null)
        {
            message.append("\n").append(ChatColor.RED).append("Reason: ").append(ChatColor.GOLD)
                    .append(ChatColor.translateAlternateColorCodes('&', reason));
        }

        if (by != null)
        {
            message.append("\n").append(ChatColor.RED).append("Banned by: ").append(ChatColor.GOLD)
                    .append(by);
        }

        if (at != null)
        {
            message.append("\n").append(ChatColor.RED).append("Issued: ").append(ChatColor.GOLD)
                    .append(DATE_FORMAT.format(at));
        }

        if (getExpiryUnix() != 0)
        {
            message.append("\n").append(ChatColor.RED).append("Expires: ").append(ChatColor.GOLD)
                    .append(DATE_FORMAT.format(FUtil.getUnixDate(expiryUnix)));
        }

        return message.toString();
    }

    @Override
    public boolean equals(Object object)
    {
        if (object == null)
        {
            return false;
        }

        if (!(object instanceof Ban))
        {
            return false;
        }

        final Ban ban = (Ban)object;
        if (hasIps() != ban.hasIps()
                || hasUsername() != ban.hasUsername())
        {
            return false;
        }

        if (hasIps() && !(getIps().equals(ban.getIps())))
        {
            return false;
        }

        return !(hasUsername() && !(getUsername().equalsIgnoreCase(ban.getUsername())));
    }

    private void dedupeIps()
    {
        Set<String> uniqueIps = new HashSet<>();

        //Fancy Collections.removeIf lets you do all that while loop work in one lambda.
        ips.removeIf(s -> !uniqueIps.add(s));
    }

    public List<String> getIps()
    {
        return ips;
    }

    public String getUsername()
    {
        return username;
    }

    public void setUsername(String username)
    {
        this.username = username;
    }

    public UUID getUuid()
    {
        return uuid;
    }

    public void setUuid(UUID uuid)
    {
        this.uuid = uuid;
    }

    public String getBy()
    {
        return by;
    }

    public void setBy(String by)
    {
        this.by = by;
    }

    public Date getAt()
    {
        return at;
    }

    public void setAt(Date at)
    {
        this.at = at;
    }

    public String getReason()
    {
        return reason;
    }

    public void setReason(String reason)
    {
        this.reason = reason;
    }

    public long getExpiryUnix()
    {
        return expiryUnix;
    }

    public void setExpiryUnix(long expiryUnix)
    {
        this.expiryUnix = expiryUnix;
    }
}