package me.totalfreedom.totalfreedommod.banning;

import com.google.common.base.Strings;
import com.google.common.collect.Sets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;
import java.util.UUID;
import me.totalfreedom.totalfreedommod.FreedomService;
import me.totalfreedom.totalfreedommod.config.ConfigEntry;
import me.totalfreedom.totalfreedommod.config.YamlConfig;
import me.totalfreedom.totalfreedommod.util.FLog;
import me.totalfreedom.totalfreedommod.util.FUtil;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerLoginEvent;

public class IndefiniteBanList extends FreedomService
{

    public static final String CONFIG_FILENAME = "indefinitebans.yml";
    private YamlConfig config;

    private final Set<IndefiniteBan> indefBans = Sets.newHashSet();

    private int nameBanCount = 0;

    private int uuidBanCount = 0;

    private int ipBanCount = 0;

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy, zzzz");

    @Override
    public void onStart()
    {
        indefBans.clear();

        config = new YamlConfig(plugin, CONFIG_FILENAME, true);
        config.load();

        for (String name : config.getKeys(false))
        {
            if (!config.isConfigurationSection(name))
            {
                FLog.warning("Could not load indefinite ban for " + name + ": Invalid format!");
                continue;
            }

            IndefiniteBan indefBan = new IndefiniteBan();
            ConfigurationSection cs = config.getConfigurationSection(name);
            assert cs != null;
            indefBan.loadFrom(cs);

            if (!indefBan.isValid())
            {
                FLog.warning("Not adding indefinite ban for " + name + ": Missing information.");
                continue;
            }

            if (indefBan.isExpired())
            {
                FLog.info("Removing " + name + " from indefinite ban list as the entry has expired!");
                config.set(name, null);
                continue;
            }

            indefBans.add(indefBan);
        }

        updateCount();
        config.save();

        FLog.info("Loaded " + nameBanCount + " indefinite name bans, " + uuidBanCount + " UUID bans, and " + ipBanCount + " ip bans");
    }

    @Override
    public void onStop()
    {
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerLogin(PlayerLoginEvent event)
    {
        final String username = event.getPlayer().getName();
        final UUID uuid = event.getPlayer().getUniqueId();
        final String ip = FUtil.getIp(event);

        String bannedBy = "";
        IndefiniteBan ban = null;

        for (IndefiniteBan indefBan : indefBans)
        {
            if (username.equalsIgnoreCase(indefBan.getUsername()))
            {
                bannedBy = "username";
                ban = indefBan;
                break;
            }
            else if (indefBan.getUuid() != null && indefBan.getUuid().equals(uuid))
            {
                bannedBy = "UUID";
                ban = indefBan;
                break;
            }
            else if (indefBan.getIps().contains(ip))
            {
                bannedBy = "IP address";
                ban = indefBan;
                break;
            }
        }

        if (ban != null)
        {
            if (ban.isExpired())
            {
                config.set(ban.getUsername(), null);
                config.save();
                return;
            }

            String kickMessage = ChatColor.RED + "Your " + bannedBy + " is "
                + (ban.hasExpiry() ? "" : "indefinitely ")
                + "banned from this server.";
            String reason = ban.getReason();
            if (!Strings.isNullOrEmpty(reason))
            {
                kickMessage += "\nReason: " + ChatColor.GOLD + reason;
            }

            Date expiry = ban.getExpiry();
            if (expiry != null)
            {
                kickMessage += ChatColor.RED + "\nExpiry: " + ChatColor.GOLD + dateFormat.format(expiry);
            }

            String appealURL = ConfigEntry.SERVER_INDEFBAN_URL.getString();
            if (!Strings.isNullOrEmpty(appealURL))
            {
                kickMessage += ChatColor.RED + "\n\nRelease procedures are available at\n" + ChatColor.GOLD + ConfigEntry.SERVER_INDEFBAN_URL.getString();
            }
            event.disallow(PlayerLoginEvent.Result.KICK_OTHER, kickMessage);
        }
    }

    private void updateCount()
    {
        nameBanCount = 0;
        uuidBanCount = 0;
        ipBanCount = 0;

        for (IndefiniteBan indefBan : indefBans)
        {
            nameBanCount += 1;
            if (indefBan.getUuid() != null)
            {
                uuidBanCount += 1;
            }
            ipBanCount += indefBan.getIps().size();
        }
    }

    public Set<IndefiniteBan> getIndefBans()
    {
        return indefBans;
    }

    public int getNameBanCount()
    {
        return nameBanCount;
    }

    public int getUuidBanCount()
    {
        return uuidBanCount;
    }

    public int getIpBanCount()
    {
        return ipBanCount;
    }
}