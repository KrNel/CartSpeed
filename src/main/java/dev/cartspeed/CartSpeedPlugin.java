package dev.cartspeed;

import com.destroystokyo.paper.event.entity.EntityAddToWorldEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.FeatureFlag;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.minecart.RideableMinecart;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;
import java.util.Objects;

/**
 * Gives every plain minecart (the kind mobs and players ride) the configured top speed and empty-cart slowdown as it
 * enters a world (spawned, placed, dispensed or loaded with its chunk) and again whenever a passenger gets out, and
 * runs the lava cauldron breaker. Chest,
 * hopper, furnace, TNT, spawner and command block minecarts are left alone, so item collectors and other machines
 * keep their vanilla speeds.
 */
public final class CartSpeedPlugin extends JavaPlugin implements Listener, TabExecutor {

    private Settings settings;
    private BukkitTask cauldronTimer;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        PluginCommand command = Objects.requireNonNull(getCommand("cartspeed"), "cartspeed is missing from plugin.yml");
        command.setExecutor(this);
        command.setTabCompleter(this);
        getServer().getPluginManager().registerEvents(this, this);
        reload();
    }

    @Override
    public void onDisable() {
        if (cauldronTimer != null) {
            cauldronTimer.cancel();
        }
    }

    /** Re-reads config.yml, applies it to every loaded plain minecart and restarts the cauldron breaker. */
    private int reload() {
        reloadConfig();
        settings = Settings.from(getConfig(), getLogger());

        int carts = 0;
        for (World world : getServer().getWorlds()) {
            for (RideableMinecart cart : world.getEntitiesByClass(RideableMinecart.class)) {
                apply(cart);
                carts++;
            }
        }

        if (cauldronTimer != null) {
            cauldronTimer.cancel();
            cauldronTimer = null;
        }
        if (settings.lavaCauldronBreaksCarts()) {
            cauldronTimer = getServer().getScheduler().runTaskTimer(this, new CauldronBreaker(), 1L, 1L);
        }

        getLogger().info(describe() + " Applied to " + carts + " loaded plain minecart(s).");
        warnIfOldPhysics();
        return carts;
    }

    @EventHandler
    public void onEntityAdd(EntityAddToWorldEvent event) {
        if (event.getEntity() instanceof RideableMinecart cart) {
            apply(cart);
        }
    }

    /**
     * Applies the settings again when a mob or player gets out of a plain minecart, including a mob thrown off by an
     * activator rail. Some plugins reset a cart's top speed then: Fast Player Minecarts sets it to its own default
     * speed, which would slow farm carts for the rest of their trip. This runs after them, at HIGHEST.
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onVehicleExit(VehicleExitEvent event) {
        if (event.getVehicle() instanceof RideableMinecart cart) {
            apply(cart);
        }
    }

    private void apply(RideableMinecart cart) {
        cart.setMaxSpeed(settings.cartSpeedPerTick());
        cart.setSlowWhenEmpty(settings.slowEmptyCarts());
    }

    private String describe() {
        return "Cart speed " + Settings.trim(settings.cartSpeed()) + " blocks/s, empty carts "
                + (settings.slowEmptyCarts() ? "slow down faster (vanilla)" : "coast like full ones")
                + ", lava cauldron breaker " + (settings.lavaCauldronBreaksCarts() ? "on" : "off") + ".";
    }

    /** Above 8 blocks/s the old minecart physics can throw carts off the track at corners. */
    private void warnIfOldPhysics() {
        if (settings.cartSpeed() <= Settings.VANILLA_SPEED) {
            return;
        }
        for (World world : getServer().getWorlds()) {
            if (!world.getFeatureFlags().contains(FeatureFlag.MINECART_IMPROVEMENTS)) {
                getLogger().warning("The experimental minecart physics (minecart_improvements) is off in " + world.getName()
                        + ". cart-speed above " + Settings.trim(Settings.VANILLA_SPEED)
                        + " is meant for it; with the old physics carts can come off the track at corners.");
                return;
            }
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(Component.text("CartSpeed: " + describe(), NamedTextColor.GREEN));
            return true;
        }
        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            int carts = reload();
            sender.sendMessage(Component.text("CartSpeed reloaded. " + describe() + " Applied to " + carts
                    + " loaded plain minecart(s).", NamedTextColor.GREEN));
            return true;
        }
        sender.sendMessage(Component.text("Usage: /" + label + " [reload]", NamedTextColor.RED));
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return args.length == 1 && "reload".startsWith(args[0].toLowerCase()) ? List.of("reload") : List.of();
    }
}
