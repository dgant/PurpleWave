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

Validation for this change: all 22 checks pass. Running the same compiled tests with the original changed production classes from the base revision placed first on the classpath produces 21 assertion failures; only the unchanged rectangle control passes. This is a targeted regression suite, not a gameplay or performance benchmark. The small build-order timing, Barracks placement, and scout-recruitment changes additionally rely on caller/contract review.
