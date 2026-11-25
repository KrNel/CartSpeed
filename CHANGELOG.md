# Changelog

## 1.3.0 (2026-11-25)

### Added

- `reattach-falling-carts` (on by default): a minecart that goes off a ledge is pulled onto a rail up to 2 blocks
  below it, as the old minecart physics did. The experimental physics only catches a cart within about 1 block of a
  rail, so carts fly further before landing, and further the faster they go. This applies to every kind of
  minecart, and carries the cart's speed and riders with it. A cart isn't moved if another minecart is already in
  the way, so jams don't push carts off the track.

### Upgrading

- Your existing `config.yml` won't have the new setting, and it's on unless you add
  `reattach-falling-carts: false`. Builds made for the experimental physics that rely on carts flying off a ledge
  over rails within 2 blocks below will now see those carts caught instead.

## 1.2.0 (2026-11-25)

### Changed

- The lava cauldron breaker now breaks every kind of minecart, as lava does in vanilla, not only plain minecarts:
  - Chest, hopper and furnace minecarts drop as themselves. Spawner and command block minecarts drop a plain
    minecart, as in vanilla.
  - Chest and hopper minecarts also drop everything inside them, straight down like the minecart.
  - TNT minecarts are lit and explode, as lava does to them in vanilla.
- The breaker follows the `entity_drops` game rule: with it off, broken carts drop nothing, as in vanilla.

The speed settings still only change plain minecarts.

### Upgrading

- A chest, hopper or other minecart whose track ends at a lava cauldron will now be broken there, as it would be
  in vanilla without the experimental physics. Move any such track away from the cauldron if it should keep
  running, or set `lava-cauldron-breaks-carts: false`.

## 1.1.1 (2026-11-19)

- No changes from 1.1.0. Released under a new version number so it could be uploaded to CurseForge as the main
  file.

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
