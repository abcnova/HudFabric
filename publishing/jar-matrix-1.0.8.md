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

All 17 individual target projects passed `clean build` on 2026-10-22. Each range JAR is packaged from the oldest version in its group; the Minecraft dependency range is embedded in `fabric.mod.json`. These builds confirm source compilation for every listed target, but do not replace an in-game runtime test on each version.

The 26.3 client game test reached client startup after the key-category fix, then the Java process exited with Windows status `0xC0000005` before reporting test results. Module dragging therefore remains unverified in a live 26.3 client. The GitHub release should remain a prerelease for this user-facing check.

Final artifacts are stored in `artifacts/1.0.8-beta/`. Do not include `*-sources.jar` or `*-dev.jar` in the release assets.
