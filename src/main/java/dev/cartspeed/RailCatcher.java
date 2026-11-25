package dev.cartspeed;

import io.papermc.paper.entity.TeleportFlag;
import org.bukkit.Bukkit;
import org.bukkit.FeatureFlag;
import org.bukkit.Location;
import org.bukkit.Tag;
import org.bukkit.World;
import org.bukkit.entity.Minecart;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

/**
 * Runs every tick and pulls falling minecarts onto a rail up to two blocks below them, as the old minecart physics did.
 * <p>
 * The old physics looked for a rail in the cart's own block and in the block below it, so a cart that went off a
 * ledge was caught by a rail up to two blocks lower almost at once. The experimental physics only looks just under
 * the cart, about one block, so a cart falls further in an arc before it reaches a rail. This catches the carts the
 * old physics would have caught: no rail where the experimental physics looks, but a rail in the block below the
 * cart's. The cart is moved down onto that rail, keeping its speed and riders, and the game attaches it on its tick.
 * <p>
 * A cart is left where it is if another minecart is already in the space it would be moved to. In a jam, carts get
 * pushed up on top of each other, and moving one down into another shoves carts off the track.
 */
final class RailCatcher implements Runnable {

    /** Where the experimental physics looks for a rail: this far under the cart's bottom. */
    private static final double NEW_PHYSICS_REACH = 0.1 + 1.0E-5;

    @Override
    public void run() {
        for (World world : Bukkit.getWorlds()) {
            if (!world.getFeatureFlags().contains(FeatureFlag.MINECART_IMPROVEMENTS)) {
                continue; // the old physics catches carts itself
            }
            for (Minecart cart : world.getEntitiesByClass(Minecart.class)) {
                if (cart.isValid() && !cart.isInsideVehicle()) {
                    catchOnRail(world, cart);
                }
            }
        }
    }

    private static void catchOnRail(World world, Minecart cart) {
        Location at = cart.getLocation();
        int x = at.getBlockX();
        int y = at.getBlockY();
        int z = at.getBlockZ();
        if (isRail(world, x, y, z) || isRail(world, x, (int) Math.floor(at.getY() - NEW_PHYSICS_REACH), z)) {
            return; // on a rail, or close enough that the experimental physics attaches it itself
        }
        if (!isRail(world, x, y - 1, z)) {
            return;
        }
        at.setY(y - 1);
        if (occupied(world, cart, at)) {
            return; // pushed up onto another cart, as in a jam: moving it down would shove the other off the track
        }
        Vector velocity = cart.getVelocity();
        if (cart.teleport(at, TeleportFlag.EntityState.RETAIN_PASSENGERS)) {
            cart.setVelocity(velocity);
        }
    }

    /** Whether another minecart is in the space the cart would be moved to. */
    private static boolean occupied(World world, Minecart cart, Location target) {
        BoundingBox space = cart.getBoundingBox().shift(0, target.getY() - cart.getLocation().getY(), 0);
        return !world.getNearbyEntities(space, other -> other instanceof Minecart && other != cart).isEmpty();
    }

    private static boolean isRail(World world, int x, int y, int z) {
        return Tag.RAILS.isTagged(world.getBlockAt(x, y, z).getType());
    }
}
