# Changelog

## 1.1.0 (2026-11-19)

### Changed

- CartSpeed now only changes plain minecarts, the kind mobs and players ride. Chest, hopper, furnace, TNT,
  spawner and command block minecarts are left alone and follow the `max_minecart_speed` game rule as in
  vanilla. Hopper minecart item collectors no longer run at CartSpeed's speed.
- The startup message and `/cartspeed reload` now count plain minecarts only.

### Fixed

- Works alongside [Fast Player Minecarts](https://modrinth.com/plugin/lynx-minecarts) (Lynx Minecarts). That
  plugin resets a minecart's top speed to its `defaultSpeed` whenever a passenger gets out, which slowed farm
  carts for the rest of their trip after the mob was dropped off. CartSpeed now restores its speed whenever a
  mob or player gets out of a plain minecart, including mobs thrown off by an activator rail. Fast Player
  Minecarts' settings don't need changing.

### Upgrading

- Replace the 1.0.0 jar with `CartSpeed-1.1.0.jar` and restart the server. The settings in `config.yml` are
  unchanged, so your existing config keeps working.
- Hopper and other minecarts that 1.0.0 sped up return to normal speed after the restart, because a cart's
  speed isn't saved with it.

## 1.0.0 (2026-11-05)

First release, for Paper 26.3 with the experimental minecart physics.

- `cart-speed`: the top speed of every minecart, from 1 to 1000 blocks/s. 8 is vanilla.
- `slow-empty-carts`: set to `false` to let empty minecarts coast like ones carrying a mob, so lines of empty
  and mob-carrying carts keep their spacing instead of jamming.
- `lava-cauldron-breaks-carts`: a minecart pressed against a lava cauldron at the end of its track breaks, and
  the minecart drops straight down for a hopper to collect.
- `/cartspeed` shows the current settings and `/cartspeed reload` re-applies `config.yml` (permission
  `cartspeed.admin`).
- Logs a warning if `cart-speed` is above 8 in a world without the experimental physics.
