package dev.cartspeed;

import org.bukkit.configuration.file.FileConfiguration;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.logging.Logger;

/** What config.yml says. Speeds are in blocks per second. */
record Settings(double cartSpeed, boolean slowEmptyCarts, boolean lavaCauldronBreaksCarts) {

    static final double VANILLA_SPEED = 8.0;
    /** The slowest and fastest the max_minecart_speed game rule allows; the fastest is the experimental physics' limit. */
    static final double SLOWEST_SPEED = 1.0;
    static final double FASTEST_SPEED = 1000.0;
    static final double TICKS_PER_SECOND = 20.0;

    static Settings from(FileConfiguration config, Logger logger) {
        return new Settings(cartSpeed(config, logger), config.getBoolean("slow-empty-carts", false),
                config.getBoolean("lava-cauldron-breaks-carts", true));
    }

    private static double cartSpeed(FileConfiguration config, Logger logger) {
        Object value = config.get("cart-speed");
        if (value == null) {
            return VANILLA_SPEED;
        }
        if (!(value instanceof Number number) || Double.isNaN(number.doubleValue())) {
            logger.warning("cart-speed '" + value + "' is not a number; using " + trim(VANILLA_SPEED) + " blocks/s.");
            return VANILLA_SPEED;
        }
        double speed = number.doubleValue();
        double clamped = Math.max(SLOWEST_SPEED, Math.min(FASTEST_SPEED, speed));
        if (clamped != speed) {
            logger.warning("cart-speed " + trim(speed) + " is outside " + trim(SLOWEST_SPEED) + " to " + trim(FASTEST_SPEED)
                    + " blocks/s; using " + trim(clamped) + ".");
        }
        return clamped;
    }

    /** The cart speed in blocks per tick, the unit minecarts use. */
    double cartSpeedPerTick() {
        return cartSpeed / TICKS_PER_SECOND;
    }

    static String trim(double value) {
        return new BigDecimal(String.format(Locale.ROOT, "%.2f", value)).stripTrailingZeros().toPlainString();
    }
}
