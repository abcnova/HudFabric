# Changelog

## 1.0.7

- Fixed module dragging on Minecraft 26.3 and verified it with a client game test.
- Restored the familiar settings layout and improved narrow-window handling.
- Added per-module, crosshair, extra-settings, and full configuration resets.
- Kept HUD elements inside the visible canvas when window dimensions change.
- Cleaned unsupported glyphs from custom scoreboard titles.
- Clarified the Jump Reset Indicator Background and Text color controls.

## 1.0.6

- Added full Minecraft 26.3 support.
- Reworked the Jump Reset Indicator to react only to real local jump events
  during active player combat.
- Prevented normal movement and unrelated incoming damage from triggering the
  indicator.
- Added early, perfect, late, and missed timing feedback.
- Renamed every public setting from “Jump Reset” to “Jump Reset Indicator”.
- Updated Fabric Loader, Fabric API, and Mod Menu compatibility for every
  supported Minecraft version.
- Rebuilt and verified all six release artifacts.

## 1.0.5

- Added CPS, crosshair indicator, hotbar-style armor durability, and the first
  Jump Reset Indicator implementation.
- Improved the HUD editor, module settings, defaults, and localization.
