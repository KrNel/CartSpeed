# CartSpeed

A Paper plugin for minecart mob farms, such as wither skeleton (WiSke) train farms, on Minecraft 26.3 with the
experimental minecart physics. It sets how fast plain minecarts can go, lets empty ones coast like full ones so
lines of carts don't jam, and makes lava cauldrons break them again.

## Features

- **Cart speed:** set the top speed of plain minecarts, from 1 to 1000 blocks/s. 8 is vanilla, and 1000 is the
  most the experimental physics allows.
- **No extra slowdown for empty carts:** empty minecarts coast like ones carrying a mob, so carts keep their
  spacing instead of catching up with each other and colliding.
- **Lava cauldron breaker:** a minecart that runs into a lava cauldron at the end of its track breaks, and the
  minecart drops straight down for a hopper to collect.
- **Other minecarts left alone:** only plain minecarts, the kind mobs and players ride, are changed. Chest,
  hopper, furnace, TNT, spawner and command block minecarts keep vanilla behaviour, so item collectors and other
  minecart machines work as before.

## Requirements

- Paper 26.3 (built and tested against build 241)
- Java 25 or newer
- The experimental minecart physics (the Minecart Improvements experiment) in the worlds with your farms.
  Experiments can only be turned on when a world is created: on a server, set
  `initial-enabled-packs=vanilla,minecart_improvements` in `server.properties` before the world is first
  generated.

CartSpeed is built for the experimental physics and has only been tested with it. If `cart-speed` is above 8
and a world doesn't have the experimental physics, CartSpeed logs a warning: the old physics can throw fast
carts off the track at corners.

## Why empty carts jam

With the experimental physics, an empty minecart loses 2.5% of its speed every tick and one carrying a mob
loses 0.3%. Mob carts end up faster than the empty carts in front of them, catch up, hit them and stop dead.
That is the jam in WiSke train farms. With `slow-empty-carts: false`, every cart slows down the same way, so
carts keep the spacing the dispenser gives them and never meet.

- **Plain rail with a booster every 9 blocks:** between boosters, an empty cart loses about 40% of its speed
  and a mob cart about 6%.
- **Powered rail all the way round:** each tick a powered rail adds 1.2 blocks/s and friction takes its share.
  For an empty cart the two cancel out at about 48 blocks/s (2.5% of 48 is 1.2), so empty carts never go
  faster than that, whatever `cart-speed` is. Mob carts wouldn't level off until 400 blocks/s, so they keep
  speeding up. In testing, empty carts topped out at 44 blocks/s while mob carts reached 69. The first carts
  collided after 6 seconds, and after a minute 102 of the 127 minecarts were stuck on the track.

## Installation

1. Put `CartSpeed-1.1.0.jar` in your server's `plugins/` folder.
2. Restart the server.
3. Edit `plugins/CartSpeed/config.yml`, then run `/cartspeed reload`.

`cart-speed` starts at 8, the vanilla limit, so raise it for faster carts.

CartSpeed is built to work alongside plugins that only speed up carts with a player in them, such as
[Fast Player Minecarts](https://modrinth.com/plugin/lynx-minecarts):

- **While a player rides:** that plugin sets the cart's speed.
- **When the player or mob gets out:** CartSpeed puts the cart back to `cart-speed`.
- **Mob farms:** Fast Player Minecarts resets a cart to its `defaultSpeed` whenever anything gets out, including
  a mob thrown off by an activator rail. CartSpeed undoes that, so farm carts keep their speed after the drop-off.
  You can leave its `defaultSpeed` at 8, the speed it slows player carts to at corners.

Don't run CartSpeed alongside a plugin that sets the speed of every minecart or breaks minecarts at lava
cauldrons. They would fight over the same carts.

## Configuration

`plugins/CartSpeed/config.yml`:

| Setting | Default | What it does |
| --- | --- | --- |
| `cart-speed` | `8` | Top speed of plain minecarts in blocks/s, from 1 to 1000. 8 is vanilla. |
| `slow-empty-carts` | `false` | `true` is vanilla: empty carts lose speed faster than carts with something riding them. |
| `lava-cauldron-breaks-carts` | `true` | Break minecarts that run into a lava cauldron, dropping the minecart straight down. |

## Commands and permissions

| Command | What it does |
| --- | --- |
| `/cartspeed` | Shows the current settings. |
| `/cartspeed reload` | Re-reads `config.yml` and applies it to every loaded plain minecart. |

Both need `cartspeed.admin`, which operators have by default.

## How it works

- Each plain minecart gets the speed limit and the empty-cart setting as it enters a world: when it's placed,
  dispensed, spawned or loaded with its chunk. `/cartspeed reload` applies them to every loaded plain minecart.
  This covers every world, and replaces the `max_minecart_speed` game rule for plain minecarts. Other kinds of
  minecart still follow the game rule and slow down when empty, as in vanilla.
- When a mob or player gets out of a plain minecart, including a mob thrown off by an activator rail, CartSpeed
  applies its settings to the cart again. It does this after other plugins have reacted, so a plugin that resets
  the cart's speed at that moment doesn't leave it slowed down.
- `cart-speed` is a limit, not a set speed. Carts only reach it where powered rails keep pushing them, and a
  powered rail adds about 1.2 blocks/s on top while a cart is on it, so 8 allows up to 9.2. Carts in water
  are limited to half.
- With the experimental physics, a minecart stops against a lava cauldron's wall and never touches the lava,
  so the usual lava cauldron cart breakers stopped working. Every tick, CartSpeed breaks any plain minecart
  (not a chest, hopper, furnace or TNT minecart) pressed against a lava cauldron just past the end of its
  rail:
  - The minecart item drops straight down with no random throw, so a hopper under the last rail catches it.
  - The item keeps the cart's custom name.
  - A cart carrying a player is broken too, as lava used to do.
  - The break fires a `VehicleDestroyEvent`, so protection plugins can cancel it.

## Tips for fast farms

- **Corners:** in testing, carts going 76 blocks/s flew off a sloped corner, up the slope's raised end and
  over the slab past it. Other sloped corners held at up to 64 blocks/s. Twelve glass blocks walling in the
  raised end fixed it: carts now stop against the glass for a tick and roll on down the slope. If carts leave
  the track somewhere, wall in the sides where they leave.
- **Timed drop-offs:** a detector rail that briefly removes a block to drop the mob can't keep up when the next
  cart arrives before its redstone resets. Without it, mobs thrown onto the block pile up until
  `max_entity_cramming` kills them. That isn't a player kill, so they drop no skulls. A fast farm needs a kill
  chamber that doesn't depend on timing.

## Test results

Tested on Paper 26.3 (build 241) with the experimental minecart physics, on copies of a WiSke train farm with a
lava cauldron cart breaker and a new wither skeleton every 1.5 seconds. The drop-off's detector rail was
disabled in every run (see "Timed drop-offs" above). Two track layouts:

- **All powered:** powered rail all the way round, sloped powered-rail corners and 127 minecarts, with glass
  added at one corner.
- **Plain with boosters:** plain rail with a booster every 9 or so blocks, sloped powered-rail corners and
  108 minecarts.

| Track | Settings | Run | Carts | Cart contacts | Speed |
| --- | --- | --- | --- | --- | --- |
| All powered | `cart-speed: 1000`, `slow-empty-carts: false` | 4 min | 614 sent out and 602 broken and collected; each minecart went round about 5 times | 0 | Mob and empty carts the same on every block; top 77 b/s |
| All powered | `cart-speed: 1000`, `slow-empty-carts: true` (vanilla) | 1 min | 164 sent out, 62 back; 102 stuck on the track | 1,716 | Empty carts top 44 b/s, mob carts 69 |
| Plain with boosters | `cart-speed: 1000`, `slow-empty-carts: false` | 2 min | 149 of 158 round the loop, 6.6 s trip | 0 | Mob and empty carts the same on every block; top 46 b/s |
| Plain with boosters | `cart-speed: 8`, `slow-empty-carts: false`\* | 8 min | 581 of 608 round the loop, 21.2 s trip | 0 | 7.9 b/s, top 9.2 |

\* Set by a test plugin before CartSpeed was written, with the same cauldron breaker.

With `slow-empty-carts: false`, every cart that reached the cauldron was broken and its minecart collected.
None were lost or stuck, and the dispenser set the pace, not the track. On the plain track, carts never went
faster than 46 blocks/s: a booster every 9 blocks only makes up for friction at about that speed.

## Building

Needs JDK 25 or newer and Maven:

```bash
mvn package
```

The jar is written to `target/CartSpeed-1.1.0.jar`.

## License

CartSpeed is released under the [MIT License](LICENSE).
