# HudFabric 1.0.8 JAR matrix

Nine compatibility JARs cover the requested Minecraft versions from 1.21 through 26.3. The group number is intentionally omitted from the first filename; subsequent files use groups 2–9. Each JAR declares the same supported Minecraft range as its filename.

| Group | Release filename | Minecraft compatibility | Build source |
|---:|---|---|---|
| 1 | `HudFabric-1.0.8-1.21-1.21.1.jar` | 1.21–1.21.1 | `versions/1.21` |
| 2 | `HudFabric-1.0.8-2-1.21.2-1.21.5.jar` | 1.21.2–1.21.5 | `versions/1.21.2` |
| 3 | `HudFabric-1.0.8-3-1.21.6-1.21.7.jar` | 1.21.6–1.21.7 | `versions/1.21.6` |
| 4 | `HudFabric-1.0.8-4-1.21.8.jar` | 1.21.8 | `versions/1.21.8` |
| 5 | `HudFabric-1.0.8-5-1.21.9-1.21.10.jar` | 1.21.9–1.21.10 | `versions/1.21.9` |
| 6 | `HudFabric-1.0.8-6-1.21.11.jar` | 1.21.11 | `versions/1.21.11` |
| 7 | `HudFabric-1.0.8-7-26.1-26.1.2.jar` | 26.1–26.1.2 | `versions/26.1` |
| 8 | `HudFabric-1.0.8-8-26.2.jar` | 26.2 | `versions/26.2` |
| 9 | `HudFabric-1.0.8-9-26.3.jar` | 26.3 | `versions/26.3` |

All nine grouped release source projects passed Gradle `build` on 2026-10-25. Each range JAR is packaged from the oldest version in its group; the Minecraft dependency range is embedded in `fabric.mod.json`.

The Minecraft 26.3 client GameTest passed with SDL3's left-button value and verified the stats module moved from `(20, 60)` to `(65, 90)` with the updated position saved. The automated client test is not a substitute for manual interaction testing on every Minecraft release.

Final artifacts are stored in `artifacts/1.0.8/`. Do not include `*-sources.jar` or `*-dev.jar` in the release assets.
