package dev.cartspeed;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Rail;

import java.util.EnumMap;
import java.util.Map;

import static org.bukkit.block.BlockFace.EAST;
import static org.bukkit.block.BlockFace.NORTH;
import static org.bukkit.block.BlockFace.SOUTH;
import static org.bukkit.block.BlockFace.WEST;

/** Reads rail shapes: which rail a cart is on, and what is just past the end of it. */
final class TrackScanner {

    /** One end of a rail piece: the side it joins, and whether that end is raised a block (slopes). */
    private record End(BlockFace face, boolean raised) {}

    /** A minecart is 0.98 blocks long; a little extra so a cart stopped against a block still counts. */
    private static final double HALF_CART_LENGTH = 0.55;

    private static final Map<Rail.Shape, End[]> ENDS = new EnumMap<>(Rail.Shape.class);

    static {
        ENDS.put(Rail.Shape.NORTH_SOUTH, ends(NORTH, false, SOUTH, false));
        ENDS.put(Rail.Shape.EAST_WEST, ends(EAST, false, WEST, false));
        ENDS.put(Rail.Shape.ASCENDING_EAST, ends(WEST, false, EAST, true));
        ENDS.put(Rail.Shape.ASCENDING_WEST, ends(EAST, false, WEST, true));
        ENDS.put(Rail.Shape.ASCENDING_NORTH, ends(SOUTH, false, NORTH, true));
        ENDS.put(Rail.Shape.ASCENDING_SOUTH, ends(NORTH, false, SOUTH, true));
        ENDS.put(Rail.Shape.SOUTH_EAST, ends(SOUTH, false, EAST, false));
        ENDS.put(Rail.Shape.SOUTH_WEST, ends(SOUTH, false, WEST, false));
        ENDS.put(Rail.Shape.NORTH_WEST, ends(NORTH, false, WEST, false));
        ENDS.put(Rail.Shape.NORTH_EAST, ends(NORTH, false, EAST, false));
    }

    private TrackScanner() {
    }

    /** The rail block a minecart at {@code location} is riding on, or null if it is not on a rail. */
    static Block railAt(Location location) {
        Block block = location.getBlock();
        if (isRail(block)) {
            return block;
        }
        // On the upper half of a slope the cart's position is already in the block above the rail.
        Block below = block.getRelative(BlockFace.DOWN);
        return isRail(below) ? below : null;
    }

    /**
     * The block of {@code material} just past one end of {@code rail} that a cart at {@code location} is
     * pressed up against (its centre within half a cart's length of that end), or null.
     */
    static Block blockPastEnd(Block rail, Location location, Material material) {
        End[] ends = endsOf(rail);
        if (ends == null) {
            return null;
        }
        for (End end : ends) {
            Block past = rail.getRelative(end.face()).getRelative(BlockFace.UP, end.raised() ? 1 : 0);
            if (past.getType() != material) {
                continue;
            }
            double toEdge = switch (end.face()) {
                case NORTH -> location.getZ() - rail.getZ();
                case SOUTH -> rail.getZ() + 1 - location.getZ();
                case WEST -> location.getX() - rail.getX();
                case EAST -> rail.getX() + 1 - location.getX();
                default -> Double.MAX_VALUE;
            };
            if (toEdge <= HALF_CART_LENGTH) {
                return past;
            }
        }
        return null;
    }

    private static boolean isRail(Block block) {
        return Tag.RAILS.isTagged(block.getType());
    }

    private static End[] endsOf(Block block) {
        if (!isRail(block)) {
            return null;
        }
        BlockData data = block.getBlockData();
        return data instanceof Rail rail ? ENDS.get(rail.getShape()) : null;
    }

    private static End[] ends(BlockFace a, boolean aRaised, BlockFace b, boolean bRaised) {
        return new End[] {new End(a, aRaised), new End(b, bRaised)};
    }
}
