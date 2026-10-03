# OpenBW storm bullet integration probe

This is a diagnostic integration test, separate from the PurpleWave regression suite. It starts short, local OpenBW simulations, casts real Psionic Storms, and compares the unmodified engine's active bullet list against its in-process BWAPI reporting. It does not run PurpleWave, deploy a bot, or benchmark ladder games.

## Tested sources and environment

Verified on October 3, 2026 with Linux/GCC 12.2 and UI disabled:

- [OpenBW/BWAPI `develop-openbw`](https://github.com/OpenBW/bwapi/tree/48124ba8ed1b4d52b3dfd52acbaf34afb9a37fe2), commit `48124ba8ed1b4d52b3dfd52acbaf34afb9a37fe2` (BWAPI 4.2).
- [OpenBW engine](https://github.com/OpenBW/openbw/tree/4b046d5f65302b10cb0a745f0fecd37ec85b20a8), commit `4b046d5f65302b10cb0a745f0fecd37ec85b20a8`.
- Local StarCraft MPQs and `(2)Destination.scx`. Neither source tree was modified.

## Reproduction

On Linux, with CMake, GCC, the two source checkouts, and legally obtained game data:

```bash
bash tests/openbw/run_storm_probe.sh \
  /path/to/bwapi /path/to/openbw /path/to/build \
  /path/to/mpqs '/path/to/maps/(2)Destination.scx'
```

The MPQ directory must contain `STARDAT.MPQ`, `BROODAT.MPQ`, and `patch_rt.mpq`. The script compiles the upstream libraries and this probe, creates an isolated runtime under the supplied build directory, and runs five scenarios with a 60-second timeout each. Do not use an existing gameplay runtime as the build directory. The engine has a 100-frame fog refresh; the spell is deliberately cast across that refresh so the fog test really loses vision.

The enemy-hidden-caster scenario changes only BWAPI's observing player after the spell is cast; it is a diagnostic view of a real simulated spell, not a two-process multiplayer test. Spawn, cast, remove, and frame-advance calls use the existing engine/BWAPI interfaces. The only private-access test seam is for the optional, separately tested client serializer.

## Results

All five in-process reporting scenarios passed:

| Scenario | Frames visible | Frames hidden | Visible frames with null source |
| --- | ---: | ---: | ---: |
| Normal spell | 65 | 0 | 0 |
| Caster removed | 65 | 0 | 59 |
| Enemy view, caster sometimes hidden | 65 | 0 | 6 |
| Own vision removed | 25 | 40 | 19 |
| Complete-map information, vision removed | 65 | 0 | 59 |

These are one run's counts, not fixed timing assertions; game initialization can vary. The probe asserts a complete spell lifecycle, correct `Psionic_Storm` type (157), correct `getPosition()` center, stable ID, raw timer agreement, zero velocity, null unit target, and disappearance after expiry. It also checks visibility against the engine tile mask and `isUnderStorm()` against the engine's unit storm timer, and observes real damage. Hidden bullets remain in the module's internal bullet set but return `exists() == false` and unknown type/position; they do not disappear prematurely while visible. A visible storm remains reportable without an accessible source unit.

### Findings and limits

1. **The core in-process storm pathway works in these builds.** `BWData.cpp` exposes `active_bullets`; `GameBullets.cpp` refreshes bullet slots; `BulletImpl.cpp` publishes type, position, visibility and lifetime. The `getPosition()` field used by PurpleWave is correct in all tested scenarios.
2. **`getTargetPosition()` is not the storm center.** Throughout these stationary spells it reports the engine's `next_movement_waypoint`, at the caster's original location. The probe checks faithful mapping to that raw field and counts the mismatch separately. Consumers should not substitute it for the tested `getPosition()` center; PurpleWave does not.
3. **`isVisible(otherPlayer)` is wrong when players have different vision.** The fork's `BulletImpl::updateData()` fills every player's visibility slot from the current player's map visibility. The fog scenario reproduced 40 mismatching frames against the other player's engine visibility mask. Current-player `exists()`/`isVisible()` still passed. This is an upstream reporting defect, not a demonstrated cause of PurpleWave's missed dodges.
4. **The standard external-client path is unfinished.** `Server::checkForConnections()` and `Server::callOnFrame()` are empty in this fork. Calling the production `Server::updateSharedMemory()` fails at `getRandomSeed()` with `ReplayHead_gameSeed_randSeed`, before bullet serialization. Thus this probe does **not** establish a working OpenBW-to-JBWAPI transport. An actual runner's custom bridge/version must be inspected separately. PurpleWave's current JBWAPI uses BWAPI 4.4; the tested OpenBW fork is 4.2.
5. **The source-null case matters for Stardust.** In the separately inspected [Stardust revision `22d93d7a`](https://github.com/bmnielsen/Stardust/blob/22d93d7a55d0a0494384474a456fd7ee26baee97/src/Bullets.cpp), bullets with neither source nor unit target are skipped before the no-go-area callback. The OpenBW probe demonstrates that a visible, damaging, position-cast storm can have exactly those fields. It does not prove that the user's observed Stardust games used this build or first encountered a storm while its source was inaccessible. PurpleWave's storm handling has no such filter.

To reproduce the external-client failure separately, after the normal run:

```bash
cd /path/to/build/storm-runtime
PROBE_SERIALIZER=1 ../StormBulletProbe '/path/to/maps/(2)Destination.scx' normal
# Expected on the tested upstream fork: exit 1, FAIL ReplayHead_gameSeed_randSeed
```

Relevant upstream implementation: [raw OpenBW adapters](https://github.com/OpenBW/bwapi/blob/48124ba8ed1b4d52b3dfd52acbaf34afb9a37fe2/bwapi/OpenBWData/BW/BWData.cpp), [bullet enumeration](https://github.com/OpenBW/bwapi/blob/48124ba8ed1b4d52b3dfd52acbaf34afb9a37fe2/bwapi/BWAPI/Source/BWAPI/GameBullets.cpp), [bullet publication](https://github.com/OpenBW/bwapi/blob/48124ba8ed1b4d52b3dfd52acbaf34afb9a37fe2/bwapi/BWAPI/Source/BWAPI/BulletImpl.cpp), and [client server](https://github.com/OpenBW/bwapi/blob/48124ba8ed1b4d52b3dfd52acbaf34afb9a37fe2/bwapi/BWAPI/Source/BWAPI/Server.cpp).

No OpenBW or Stardust production changes are included in this PurpleWave PR. The user's exact runner/replay has not been identified. The inspected local PurpleBench configuration uses StarCraft under Wine for simulation and OpenBW for replay viewing; watching such a replay does not exercise the OpenBW BWAPI bridge.
