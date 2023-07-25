package me.totalfreedom.totalfreedommod.bridge;

import java.util.*;
import java.util.concurrent.CompletableFuture;

import me.totalfreedom.totalfreedommod.FreedomService;
import me.totalfreedom.totalfreedommod.util.FLog;
import me.totalfreedom.totalfreedommod.util.FUtil;
import net.coreprotect.CoreProtect;
import net.coreprotect.CoreProtectAPI;
import net.coreprotect.utility.Util;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

public class CoreProtectBridge extends FreedomService
{
    //-- Block Inspector --//
    private static final Component name = Component.text("Block Inspector").color(TextColor.color(0x30ade4));
    private static final Component header = Component.text("---- ").append(name)
            .append(Component.text(" ---- ")).colorIfAbsent(NamedTextColor.WHITE);
    private static final Component prefix = name.append(Component.text(" - ").color(NamedTextColor.WHITE))
            .colorIfAbsent(NamedTextColor.WHITE);
    //--
    private final HashMap<UUID, Long> cooldownMap = new HashMap<>();
    private HashMap<UUID, FUtil.PaginationList<CoreProtectAPI.ParseResult>> historyMap;

    //---------------------//
    private CoreProtectAPI coreProtectAPI = null;

    public static Long getSecondsLeft(long prevTime, int timeAdd)
    {
        return prevTime / 1000L + timeAdd - System.currentTimeMillis() / 1000L;
    }

    @Override
    public void onStart()
    {
        if (isEnabled())
        {
            historyMap = new HashMap<>();
        }
    }

    @Override
    public void onStop()
    {
    }

    public CoreProtect getCoreProtect()
    {
        CoreProtect coreProtect = null;
        try
        {
            final Plugin coreProtectPlugin = server.getPluginManager().getPlugin("CoreProtect");
            assert coreProtectPlugin != null;
            if (coreProtectPlugin instanceof CoreProtect)
            {
                coreProtect = (CoreProtect)coreProtectPlugin;
            }
        }
        catch (Exception ex)
        {
            FLog.severe(ex);
        }
        return coreProtect;
    }

    public CoreProtectAPI getCoreProtectAPI()
    {
        if (coreProtectAPI == null)
        {
            try
            {
                final CoreProtect coreProtect = getCoreProtect();

                coreProtectAPI = coreProtect.getAPI();

                // Check if the plugin or api is not enabled, if so, return null
                if (!coreProtect.isEnabled() || !coreProtectAPI.isEnabled())
                {
                    return null;
                }
            }
            catch (Exception ex)
            {
                FLog.severe(ex);
            }
        }

        return coreProtectAPI;
    }

    public boolean isEnabled()
    {
        if (!server.getPluginManager().isPluginEnabled("CoreProtect"))
        {
            return false;
        }

        final CoreProtect coreProtect = getCoreProtect();

        return coreProtect != null && coreProtect.isEnabled();
    }

    // Rollback the specified player's edits that were in the last 24 hours.
    public void rollback(final String name)
    {
        if (!isEnabled())
        {
            return;
        }

        final CoreProtectAPI coreProtect = getCoreProtectAPI();

        new BukkitRunnable()
        {
            @Override
            public void run()
            {
                coreProtect.performRollback(86400, Collections.singletonList(name), null, null, null, null, 0, null);
            }
        }.runTaskAsynchronously(plugin);
    }

    // Reverts a rollback for the specified player's edits that were in the last 24 hours.
    public void restore(final String name)
    {
        if (!isEnabled())
        {
            return;
        }

        final CoreProtectAPI coreProtect = getCoreProtectAPI();

        new BukkitRunnable()
        {
            @Override
            public void run()
            {
                coreProtect.performRestore(86400, Collections.singletonList(name), null, null, null, null, 0, null);
            }
        }.runTaskAsynchronously(plugin);
    }

    public boolean hasHistory(Player player)
    {
        return historyMap.containsKey(player.getUniqueId());
    }

    public FUtil.PaginationList<CoreProtectAPI.ParseResult> getHistoryForPlayer(Player player)
    {
        return historyMap.get(player.getUniqueId());
    }

    public void showPageToPlayer(Player player, FUtil.PaginationList<CoreProtectAPI.ParseResult> results, int pageNum)
    {
        if (player == null || !player.isOnline())
        {
            return;
        }

        List<CoreProtectAPI.ParseResult> page = results.getPage(pageNum);

        if (page == null || page.isEmpty())
        {
            player.sendMessage(prefix.append(Component.text("No results were found.", NamedTextColor.WHITE)));
        }
        else
        {
            // This shouldn't change at all in any of the other entries, so this should be safe
            Component location = Component.text(String.format("(%s, %s, %s)", results.get(0).getX(),
                    results.get(0).getY(), results.get(0).getZ()));
            final long time = System.currentTimeMillis() / 1000;

            player.sendMessage(header.append(location.color(NamedTextColor.GRAY).decorate(TextDecoration.ITALIC)));
            page.forEach(entry ->
            {
                TextComponent.Builder line = Component.text();

                // Time
                line.append(Component.text(Util.getTimeSince(entry.getTime(), time, false))
                        .color(NamedTextColor.GRAY));

                // Action
                Component action = Component.text(" interacted with ");
                Component symbol = Component.text(" - ", NamedTextColor.WHITE);
                switch (entry.getActionId())
                {
                    case 0 ->
                    {
                        action = Component.text(" broke ");
                        symbol = Component.text(" - ", NamedTextColor.RED);
                    }
                    case 1 ->
                    {
                        action = Component.text(" placed ");
                        symbol = Component.text(" + ", NamedTextColor.GREEN);
                    }
                    case 2 -> action = Component.text(" clicked ");
                    default ->
                    {
                        // Do nothing (shuts Codacy up)
                    }
                }
                // Symbol, player, action, block
                line.append(symbol).append(Component.text(entry.getPlayer()).color(TextColor.color(0x30ade4)))
                        .append(action.color(NamedTextColor.WHITE)).append(
                                Component.text(entry.getBlockData().getMaterial().name().toLowerCase())
                                .color(TextColor.color(0x30ade4)));

                // Rolled back?
                if (entry.isRolledBack())
                {
                    line.decorate(TextDecoration.STRIKETHROUGH);
                }

                player.sendMessage(line.append(Component.text(".", NamedTextColor.WHITE)).build());
            });

            if (results.getPageCount() > 1)
            {
                player.sendMessage(Component.text("-----", NamedTextColor.WHITE));

                // Page indicator
                TextComponent.Builder indicator = Component.text();

                // <-
                if (pageNum > 1)
                {
                    indicator.append(Component.text("◀ ", NamedTextColor.WHITE).clickEvent(
                            ClickEvent.runCommand("/ins history " + (pageNum - 1))));
                }

                // Page <current>/<total>
                indicator.append(Component.text("Page ", TextColor.color(0x30ade4)).append(Component.text(pageNum + "/"
                        + results.getPageCount(), NamedTextColor.WHITE)));

                // ->
                if (pageNum < results.getPageCount())
                {
                    indicator.append(Component.text(" ▶", NamedTextColor.WHITE).clickEvent(
                            ClickEvent.runCommand("/ins history  " + (pageNum + 1))));
                }

                // | Use /ins history <page> for advanced navigation
                indicator.append(Component.text(" | ", NamedTextColor.GRAY).append(Component.text("Use ", NamedTextColor.WHITE)
                        .append(Component.text("/ins history <page>", TextColor.color(0x30ade4))
                                .clickEvent(ClickEvent.suggestCommand("/ins history ")))
                        .append(Component.text(" for advanced navigation", NamedTextColor.WHITE))));

                player.sendMessage(indicator.build());
            }
        }
    }

    public CompletableFuture<FUtil.PaginationList<CoreProtectAPI.ParseResult>> lookupForPlayer(Block block, Player player)
    {
        cooldownMap.put(player.getUniqueId(), System.currentTimeMillis());
        CoreProtectAPI api = getCoreProtectAPI();

        return CompletableFuture.supplyAsync(() ->
        {
            historyMap.remove(player.getUniqueId());
            FUtil.PaginationList<CoreProtectAPI.ParseResult> pages = new FUtil.PaginationList<>(10);
            api.blockLookup(block, -1).forEach(stringArray -> pages.add(api.parseResult(stringArray)));
            historyMap.put(player.getUniqueId(), pages);
            return pages;
        });
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onInteract(PlayerInteractEvent event)
    {
        // The inspector only works if we have CoreProtect installed
        if (!isEnabled())
        {
            return;
        }

        Player player = event.getPlayer();

        if ((event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.LEFT_CLICK_BLOCK)
                && plugin.pl.getData(player.getUniqueId()).hasInspection())
        {
            event.setCancelled(true);
            Block block = event.getClickedBlock();
            Optional<Long> cooldown = Optional.ofNullable(cooldownMap.get(player.getUniqueId()));

            if (cooldown.isPresent() && getSecondsLeft(cooldown.get(), 3) > 0L)
            {
                player.sendMessage(prefix.append(Component.text("You need to wait ")
                        .append(Component.text(getSecondsLeft(cooldown.get(), 3)))
                        .append(Component.text(" seconds before you can make another query."))
                        .color(NamedTextColor.WHITE)));
                return;
            }

            // Time to do a look-up.
            if (block != null)
            {
                /* This is a hack to make it so that when you right-click, the coordinates that get used depend on
                 *  what's in your hand. Non-blocks use the block you clicked directly, but blocks use wherever the
                 *  block was supposed to be placed. */
                ItemStack hand = player.getInventory().getItemInMainHand();
                if (event.getAction() == Action.RIGHT_CLICK_BLOCK && hand.getType().isBlock() && hand.getType() != Material.AIR)
                {
                    block = block.getRelative(event.getBlockFace()).getState().getBlock();
                }

                lookupForPlayer(block, player).thenAccept(results ->
                {
                    if (results.isEmpty())
                    {
                        player.sendMessage(prefix.append(Component.text("No results were found.").color(NamedTextColor.WHITE)));
                    }
                    else
                    {
                        showPageToPlayer(player, results, 1);
                    }
                });
            }
        }
    }
}