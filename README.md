<p align="center">
  <img src="versions/1.21.11/src/main/resources/assets/hudfabric/icon/icon.png" width="128" height="128" alt="HudFabric icon">
</p>

# HudFabric

HudFabric is a client-side Fabric HUD mod by **ABC_Nova**. It provides a clean,
modular editor for the scoreboard, FPS/ping, coordinates and biome, potion
effects, CPS, crosshair, block outline, armor/tool durability, an inventory
counter, and a movable in-game note board.

[Download on Modrinth](https://modrinth.com/mod/hudfabric) ·
[Download on CurseForge](https://www.curseforge.com/minecraft/mc-mods/hudfabric) ·
[Releases](https://github.com/abcnova/HudFabric/releases) ·
[Report an issue](https://github.com/abcnova/HudFabric/issues)

## Highlights

- Movable and scalable HUD modules
- Reset controls for individual modules, the crosshair, extra settings, and the full configuration
- Compact color editing for narrower GUI sizes
- Per-module colors, width, scale, and corner radius
- Custom scoreboard with server formatting support
- Potion effect timers and optional vanilla-effect suppression
- CPS display and custom crosshair
- Jump Reset Indicator with combat-only timing feedback
- Armor/tool durability panel
- Optional hotbar-style armor widget with left/right placement
- Bar, numeric, or percentage durability display
- Low-durability warning threshold
- Optional armor-break warning (disabled by default)
- Totem and active-arrow counter
- Editable, movable in-game note board with a Minecraft-style wood frame, cork backing, pinned paper and ruled writing lines (personal/client-side)
- Server-specific profiles and settings migration
- Auto-respawn and auto-reconnect helpers
- Copyable, privacy-safe settings codes for sharing in Discord; imports preserve local server profiles
- Repositionable pause-menu HUD Editor button with a pencil control and configurable Right Alt keybind
- Independent armor hotbar slots use Minecraft's vanilla hotbar sprite, including resource-pack overrides
- Fixed Minecraft 26.3 HUD dragging by matching SDL3's left mouse-button value; the automated client test verifies the updated module coordinates
- Replaced the generic note panel with a Minecraft-inspired pinned noticeboard design across the supported versions

## Supported Minecraft versions

All nine artifacts are published as version 1.0.8.

| Minecraft versions | Release artifact |
| --- | --- |
| 1.21–1.21.1 | `HudFabric-1.0.8-1.21-1.21.1.jar` |
| 1.21.2–1.21.5 | `HudFabric-1.0.8-2-1.21.2-1.21.5.jar` |
| 1.21.6–1.21.7 | `HudFabric-1.0.8-3-1.21.6-1.21.7.jar` |
| 1.21.8 | `HudFabric-1.0.8-4-1.21.8.jar` |
| 1.21.9–1.21.10 | `HudFabric-1.0.8-5-1.21.9-1.21.10.jar` |
| 1.21.11 | `HudFabric-1.0.8-6-1.21.11.jar` |
| 26.1–26.1.2 | `HudFabric-1.0.8-7-26.1-26.1.2.jar` |
| 26.2 | `HudFabric-1.0.8-8-26.2.jar` |
| 26.3 | `HudFabric-1.0.8-9-26.3.jar` |

## Source layout

- `versions/` – version-specific source projects for every supported game version
- `artifacts/1.0.8` – grouped release JARs
- `publishing/jar-matrix-1.0.8.md` – compatibility and verification notes
- `history` – privacy-safe archives, resources, and reconstructed Java source
  for 1.0.0 through 1.0.3

## Building

Use Java 21 for Minecraft 1.21.11 and Java 25 for Minecraft 26.x. Run the
Gradle `build` task inside the matching version directory.

## Privacy

The public project uses only the creator identity **ABC_Nova**. No private
profile information is required by the mod.

The note board is personal and saved in the local client configuration; it is
not synchronized to other players or the server.

## License

HudFabric is available under the MIT License. See `THIRD_PARTY_NOTICES.md` for
credits related to referenced open-source projects.
