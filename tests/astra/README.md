# Logic regression tests

These tests execute production Scala code with in-memory BWAPI data. They do not connect to StarCraft, issue game commands, or start a benchmark. The fixtures populate unit snapshots directly because `readProxy` requires a live client.

From the repository root, with JDK 8 and Maven available:

```powershell
git submodule update --init --recursive
New-Item -ItemType Directory -Force out/artifacts | Out-Null
mvn -B clean package
./scripts/test_astra_logic.ps1
```

The test script accepts `-JavaHome`, `-MavenRepository`, and `-Jar` overrides. It uses the Scala 2.12.18 compiler dependencies already fetched by Maven, puts test classes under `target/astra-tests`, and fails on compilation errors or failed assertions. Tests are separate from Maven's currently unconfigured test lifecycle.

Coverage includes spiral bounds and retraversal, air/ground path threat selection, opening-fingerprint timing and evidence, cancellation, hysteresis state and fractional decay, AOE coverage and per-spell scoring, matchup allegiance and minimum range, simulated Siege Tank minimum range, and enemy upgrade accounting. The unchanged rectangle-clamping test is a control: its initially suspicious argument order is mathematically correct.

The follow-up also covers armor and Medic engagement in combat prediction; changing squad composition, air-only targeting, and burrowed detection; exact-cost supply-block diagnostics; every tile of construction footprints; in-range combat-target priority; unseen Zerg morph completion; relocated building footprints; Scourge/Greater Spire production; Carrier safety and Mutalisk recovery; construction-blocker memory; the four-Pool Extractor trick; empty placement indexes; and drop interruption/evacuation.

Validation for this change: all 42 checks pass. Compiling the same test source against the original production Scala classes from base revision `f35e0129f` produces 41 failures (40 assertions and the expected empty-placement exception); only the unchanged rectangle control passes. The baseline recompiles all 938 production Scala files and the test harness so Scala trait implementation changes do not introduce binary-linkage artifacts. This is a targeted regression suite, not a gameplay or performance benchmark. The small build-order timing, Barracks placement, and scout-recruitment changes additionally rely on caller/contract review.
