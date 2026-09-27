# HudFabric

HudFabric is a clean, client-side HUD editor for Minecraft Fabric. Move, scale,
and style useful HUD modules directly in-game—no server installation required.

[Source code](https://github.com/abcnova/HudFabric) ·
[Releases](https://github.com/abcnova/HudFabric/releases) ·
[Report an issue](https://github.com/abcnova/HudFabric/issues)

## Modules

- Custom scoreboard with server formatting support
- FPS and ping display
- Coordinates and biome
- Potion effects with icons and accurate remaining time
- Armor and tool durability in panel or vanilla hotbar style
- Left/right CPS counter
- Jump Reset Indicator for PvP timing practice
- Configurable crosshair, Crosshair Indicator, and block outline

## Highlights in 1.0.6

- Added a dedicated Minecraft 26.3 build
- Replaced jump-key polling with real local jump-event detection
- Restricted Jump Reset Indicator feedback to active player combat
- Added early, perfect, late, and missed timing feedback
- Prevented ordinary movement and unrelated damage from triggering feedback
- Updated dependencies and rebuilt every supported version
- Preserved and automatically migrated existing HudFabric settings

## Compatibility

Dedicated builds are available for Minecraft 1.21.11, 26.1, 26.1.1, 26.1.2,
26.2, and 26.3. Match the downloaded JAR to the exact Minecraft version.

Requirements: Fabric Loader, Fabric API, Java 21 for 1.21.11, and Java 25 for
26.x. Mod Menu is optional.

## Languages

English, German, and Spanish are included.

HudFabric is open source under the MIT License.
