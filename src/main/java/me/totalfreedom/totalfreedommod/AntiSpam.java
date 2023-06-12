package me.totalfreedom.totalfreedommod;

import io.papermc.paper.event.player.AsyncChatEvent;
import me.totalfreedom.totalfreedommod.player.FPlayer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class AntiSpam extends FreedomService
{
    private ScheduledThreadPoolExecutor cycle;
    public static final int MSG_PER_CYCLE = 10;
    //
    private final Map<UUID, Long> chatRatelimit = new HashMap<>();

    @Override
    public void onStart()
    {
        cycle = new ScheduledThreadPoolExecutor(1);
        cycle.scheduleAtFixedRate(this::cycle, 0, 1, TimeUnit.SECONDS);
    }

    @Override
    public void onStop()
    {
        cycle.shutdownNow();
    }

    private void cycle()
    {
        server.getOnlinePlayers().stream().map(player -> plugin.pl.getPlayer(player)).forEach(fPlayer ->
        {
            // TODO: Move each to their own section
            fPlayer.resetMsgCount();
            fPlayer.resetBlockDestroyCount();
            fPlayer.resetBlockPlaceCount();
        });
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onAsyncPlayerChat(AsyncChatEvent event)
    {
        final UUID uuid = event.getPlayer().getUniqueId();
        if (chatRatelimit.get(uuid) != null) {
            final long lastChat = chatRatelimit.get(uuid);
            final long diff = System.currentTimeMillis() - lastChat;

            if (diff <= 75) {
                event.setCancelled(true);
                return;
            }
        }

        chatRatelimit.put(uuid, System.currentTimeMillis());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event)
    {
        String command = event.getMessage();
        final Player player = event.getPlayer();
        final FPlayer fPlayer = plugin.pl.getPlayer(player);
        fPlayer.setLastCommand(command);

        if (fPlayer.allCommandsBlocked())
        {
            player.sendMessage(Component.text("Your commands have been blocked by an admin.", NamedTextColor.RED));
            event.setCancelled(true);
            return;
        }

        if (fPlayer.incrementAndGetMsgCount() > MSG_PER_CYCLE)
        {
            event.setCancelled(true);
        }
    }
}
