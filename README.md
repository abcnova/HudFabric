<p align="center">
  <img src="versions/1.21.11/src/main/resources/assets/hudfabric/icon/icon.png" width="128" height="128" alt="HudFabric icon">
</p>

# HudFabric

HudFabric is a client-side Fabric HUD mod by **ABC_Nova**. It provides a clean,
modular editor for the scoreboard, FPS/ping, coordinates and biome, potion
effects, CPS, crosshair, block outline, and armor/tool durability.

[Download on Modrinth](https://modrinth.com/mod/hudfabric) ·
[Download on CurseForge](https://www.curseforge.com/minecraft/mc-mods/hudfabric) ·
[Source and releases](https://github.com/abcnova/HudFabric)

## Highlights

- Movable and scalable HUD modules
- Per-module colors, width, scale, and corner radius
- Custom scoreboard with server formatting support
- Potion effect timers and optional vanilla-effect suppression
- CPS display and custom crosshair
- Armor/tool durability panel
- Optional hotbar-style armor widget with left/right placement
- Bar, numeric, or percentage durability display
- Low-durability warning threshold
- Server-specific profiles and settings migration
- Auto-respawn and auto-reconnect helpers

## Supported Minecraft versions

| Minecraft | Artifact |
| --- | --- |
| 1.21.11 | `HudFabric-1.0.4-1.21.11.jar` |
| 26.1 | `HudFabric-1.0.4-2-26.1.jar` |
| 26.1.1 | `HudFabric-1.0.4-3-26.1.1.jar` |
| 26.1.2 | `HudFabric-1.0.4-4-26.1.2.jar` |
| 26.2 | `HudFabric-1.0.4-5-26.2.jar` |

## Source layout

- `versions/1.21.11` – Yarn-based 1.21.11 source
- `versions/26.1.x` – Mojang-mapped 26.1.x source
- `versions/26.2` – Mojang-mapped 26.2 source
- `artifacts/1.0.4` – ready-to-use release JARs
- `history` – privacy-safe archives, resources, and reconstructed Java source
  for 1.0.0 through 1.0.3

## Building

Use Java 21 for Minecraft 1.21.11 and Java 25 for Minecraft 26.x. Run the
Gradle `build` task inside the matching version directory.

## Privacy

The public project uses only the creator identity **ABC_Nova**. No private
profile information is required by the mod.

## License

HudFabric is available under the MIT License. See `THIRD_PARTY_NOTICES.md` for
credits related to referenced open-source projects.
