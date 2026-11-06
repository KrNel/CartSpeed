package dev.cartspeed;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.Item;
import org.bukkit.entity.minecart.RideableMinecart;
import org.bukkit.event.vehicle.VehicleDestroyEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

/**
 * Runs every tick and breaks minecarts that have run into a lava cauldron at the end of their track. Before the
 * experimental minecart physics the cart poked into the cauldron and burned; now it stops against the cauldron's
 * wall and never touches the lava. The minecart drops straight down, not thrown like a vanilla break, so a hopper
 * under the last rail catches it.
 */
final class CauldronBreaker implements Runnable {

    @Override
    public void run() {
        for (World world : Bukkit.getWorlds()) {
            for (RideableMinecart cart : world.getEntitiesByClass(RideableMinecart.class)) {
                if (!cart.isValid()) {
                    continue;
                }
                Location location = cart.getLocation();
                Block rail = TrackScanner.railAt(location);
                if (rail != null && TrackScanner.blockPastEnd(rail, location, Material.LAVA_CAULDRON) != null) {
                    burn(cart, location);
                }
            }
        }
    }

    private static void burn(RideableMinecart cart, Location at) {
        VehicleDestroyEvent event = new VehicleDestroyEvent(cart, DamageSource.builder(DamageType.LAVA).build(), null);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return;
        }
        ItemStack drop = new ItemStack(Material.MINECART);
        Component name = cart.customName();
        if (name != null) {
            drop.editMeta(meta -> meta.customName(name));
        }
        cart.eject();
        cart.remove();
        Item item = at.getWorld().dropItem(at, drop);
        item.setVelocity(new Vector());
        at.getWorld().playSound(at, Sound.ENTITY_GENERIC_BURN, 0.4f, 1.0f);
    }
}
