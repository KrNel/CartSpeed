package dev.cartspeed;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.GameRules;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.minecart.ExplosiveMinecart;
import org.bukkit.event.vehicle.VehicleDestroyEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Runs every tick and breaks minecarts that have run into a lava cauldron at the end of their track. Before the
 * experimental minecart physics the cart poked into the cauldron and burned; now it stops against the cauldron's
 * wall and never touches the lava. Every kind of minecart is broken, as lava breaks them in vanilla:
 * <ul>
 *   <li>The minecart drops as the same item vanilla drops (spawner and command block minecarts drop a plain
 *       minecart), keeping its custom name.</li>
 *   <li>Chest and hopper minecarts also drop everything inside them.</li>
 *   <li>TNT minecarts are lit instead, with vanilla's random fuse for lava, and explode.</li>
 * </ul>
 * Items drop straight down, not thrown like a vanilla break, so a hopper under the last rail catches them.
 */
final class CauldronBreaker implements Runnable {

    @Override
    public void run() {
        for (World world : Bukkit.getWorlds()) {
            for (Minecart cart : world.getEntitiesByClass(Minecart.class)) {
                if (!cart.isValid() || cart instanceof ExplosiveMinecart tnt && tnt.isIgnited()) {
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

    private static void burn(Minecart cart, Location at) {
        VehicleDestroyEvent event = new VehicleDestroyEvent(cart, DamageSource.builder(DamageType.LAVA).build(), null);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return;
        }
        World world = at.getWorld();
        world.playSound(at, Sound.ENTITY_GENERIC_BURN, 0.4f, 1.0f);

        if (cart instanceof ExplosiveMinecart tnt) {
            // Lava lights a TNT minecart rather than breaking it. ignite() does nothing when the tnt_explodes game
            // rule is off or a plugin cancels the ignition, and then the cart stays, as in vanilla.
            tnt.ignite();
            if (tnt.isIgnited()) {
                ThreadLocalRandom random = ThreadLocalRandom.current();
                tnt.setFuseTicks(random.nextInt(20) + random.nextInt(20));
            }
            return;
        }

        List<ItemStack> drops = new ArrayList<>();
        ItemStack minecart = new ItemStack(cart.getMinecartMaterial());
        Component name = cart.customName();
        if (name != null) {
            minecart.editMeta(meta -> meta.customName(name));
        }
        drops.add(minecart);
        if (cart instanceof InventoryHolder holder) {
            // getItem fills a chest minecart that still has its loot table, as breaking it does in vanilla.
            Inventory inventory = holder.getInventory();
            for (int slot = 0; slot < inventory.getSize(); slot++) {
                ItemStack stack = inventory.getItem(slot);
                if (stack != null && !stack.isEmpty()) {
                    drops.add(stack.clone());
                }
            }
            inventory.clear();
        }

        cart.eject();
        cart.remove();
        if (!Boolean.FALSE.equals(world.getGameRuleValue(GameRules.ENTITY_DROPS))) {
            for (ItemStack drop : drops) {
                Item item = world.dropItem(at, drop);
                item.setVelocity(new Vector());
            }
        }
    }
}
