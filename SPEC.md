# PurpleWave Packaging Specification

PurpleWave tournament packages contain a runnable `PurpleWave.jar`, BWAPI integration files, tournament configuration, revision metadata, and launcher scripts at the archive root.

The COG 2026 package is named `PurpleWaveCOG2026.zip`. It uses `PurpleWaveAIIDE.config.json`, contains the maintained `configs/run_proxy.bat`, and includes both `run32.bat` and `run64.bat` so the appropriate Java launcher is available.

The package generator sources `run_proxy.bat` from the repository. It does not depend on a Desktop file or another external launcher path at generation time.

Package generation produces a valid ZIP with no nested copy of itself. Generation fails if the archived `PurpleWave.jar` differs byte-for-byte from the ladder artifact being packaged.

