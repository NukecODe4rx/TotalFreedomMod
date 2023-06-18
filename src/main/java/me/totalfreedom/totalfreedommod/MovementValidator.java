package me.totalfreedom.totalfreedommod;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.spigotmc.event.player.PlayerSpawnLocationEvent;

public class MovementValidator extends FreedomService
{

    public static final int MAX_XYZ_COORD = 29999998;
    public static final int MAX_DISTANCE_TRAVELED = 100;

    @Override
    public void onStart()
    {
    }

    @Override
    public void onStop()
    {
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerTeleport(PlayerTeleportEvent event)
    {
        // Check absolute value to account for negatives
        if (isOutOfBounds(event.getTo()))
        {
            event.setCancelled(true); // illegal position, cancel it
        }
    }

    private boolean isOutOfBounds(final Location position)
    {
        return Math.abs(position.getX()) >= MAX_XYZ_COORD || Math.abs(position.getY()) >= MAX_XYZ_COORD || Math.abs(position.getZ()) >= MAX_XYZ_COORD;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerMove(PlayerMoveEvent event)
    {
        final Player player = event.getPlayer();
        Location from = event.getFrom();
        Location to = event.getTo();
        double distance = from.distanceSquared(to);

        if (distance >= MAX_DISTANCE_TRAVELED)
        {
            event.setCancelled(true);
            player.kick(Component.text("You were moving too quickly!", NamedTextColor.RED));
        }
        // Check absolute value to account for negatives
        if (isOutOfBounds(event.getTo()))
        {
            event.setCancelled(true);
            player.teleportAsync(player.getWorld().getSpawnLocation());
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerLogin(PlayerLoginEvent event)
    {
        final Player player = event.getPlayer();

        // Validate position
        if (isOutOfBounds(player.getLocation()))
        {
            player.teleportAsync(player.getWorld().getSpawnLocation()); // Illegal position, teleport to spawn
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerSpawn(PlayerSpawnLocationEvent event)
    {
        final Location playerSpawn = event.getSpawnLocation();
        final Location worldSpawn = event.getPlayer().getWorld().getSpawnLocation();

        // If the player's spawn is equal to the world's spawn, there is no need to check.
        // This will also prevent any possible feedback loops pertaining to setting an out of bounds world spawn to the same world spawn.
        if (playerSpawn == worldSpawn)
        {
            return;
        }

        if (isOutOfBounds(worldSpawn))
        {
            event.setSpawnLocation(worldSpawn);
        }
    }
}
