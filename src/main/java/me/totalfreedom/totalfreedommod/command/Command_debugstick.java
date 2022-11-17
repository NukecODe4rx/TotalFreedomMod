package me.totalfreedom.totalfreedommod.command;

import java.util.Arrays;
import java.util.List;
import me.totalfreedom.totalfreedommod.rank.Rank;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

@CommandPermissions(level = Rank.OP, source = SourceType.ONLY_IN_GAME)
@CommandParameters(description = "Get a stick of happiness.", usage = "/<command>")
public class Command_debugstick extends FreedomCommand
{
    @Override
    public boolean run(CommandSender sender, Player playerSender, Command cmd, String commandLabel, String[] args, boolean senderIsConsole)
    {
        ItemStack itemStack = new ItemStack(Material.DEBUG_STICK);
        ItemMeta itemMeta = itemStack.getItemMeta();
        assert itemMeta != null;
        itemMeta.displayName(Component.text("Stick of Happiness", NamedTextColor.GOLD).decorate(TextDecoration.BOLD));
        itemMeta.lore(Arrays.asList(
                Component.text("This is the most powerful stick in the game.", NamedTextColor.RED),
                Component.text("You can left click to select what you want to change.", NamedTextColor.DARK_BLUE),
                Component.text("And then you can right click to change it!", NamedTextColor.DARK_GREEN),
                Component.text("Isn't technology amazing?", NamedTextColor.DARK_PURPLE)
        ));
        itemStack.setItemMeta(itemMeta);
        playerSender.getInventory().addItem(itemStack);
        return true;
    }
}
