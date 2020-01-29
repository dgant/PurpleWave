# PurpleWave
### An extensible AI player for *StarCraft: Brood War*

https://github.com/dgant/PurpleWave

## About
PurpleWave is a *StarCraft: Brood War* AI written in Scala. It can play all races and a large variety of strategies.

PurpleWave has won:
 * 1st Place in the [2019 IEEE CoG StarCraft AI Competition](https://cilab.gist.ac.kr/sc_competition2019/?cat=17)
 * 1st Place in the [2018 SSCAIT](https://sscaitournament.com/index.php?action=2018) 
 * 1st Place in the [2018 AIST1](https://sites.google.com/view/aistarcrafttournament/aist-s1)
 * 2nd place in the [2019 AIIDE StarCraft AI Competition](https://www.cs.mun.ca/~dchurchill/starcraftaicomp/2019/)
 * 2nd Place in the [2017 AIIDE StarCraft AI Competition](https://www.cs.mun.ca/~dchurchill/starcraftaicomp/2017/)
 * 2nd Place in the [2018 IEEE CIG StarCraft AI Competition](https://cilab.gist.ac.kr/sc_competition2018/?cat=17)
 * 3rd Place in the [2017 IEEE CIG StarCraft AI Competition](https://cilab.gist.ac.kr/sc_competition2017/?cat=17)

PurpleWave vs. Iron, AIIDE 2017:

[![PurpleWave vs. Iron, AIIDE 2017](https://img.youtube.com/vi/g33PIqDdTqs/0.jpg)](https://www.youtube.com/watch?v=g33PIqDdTqs)

## Credits
Thanks to:
* Nathan Roth (Antiga/Iruian) for strategy advice and consulting -- so much of the polish in PurpleWave's strategies comes from his wisdom and replay analysis
* @jaj22/JohnJ for lots of advice navigating Brood War mechanics
* @IMP42 @AdakiteSystems and @tscmoo for helping me get BWAPI up and running when I was getting started
* @kovarex @heinermann @bgweber @certicky @krasi0 @davechurchill and the Cognition & Intelligence Lab at Sejong University for making Brood War competitions possible in the first place
* @JasperGeurtz and @Bytekeeper for JBWAPI, @vjurenka for BWMirror, Luke Perkins for BWTA, and @CMcCrave for MCRS/Horizon 

## How to build PurpleWave
[See build instructions in install.md](install/install.md)

Steps: 
* Clone or download this repository (I keep it in c:\p\pw but it should work from anywhere)
* If you cloned the repository `git submodule sync; git submodule update --init --recursive` to clone JBWAPI 
* Open IntelliJ IDEA
* In IntelliJ IDEA: File -> Settings -> Plugins -> Check off Scala
* In IntelliJ IDEA: File -> Open -> Select the PurpleWave directory
* In IntelliJ IDEA: File -> Project Structure -> Select the Java Development Kit directory (like c:\Program Files\Java\jdk\1.8.0_121)
* In IntelliJ IDEA: File -> Project Structure -> Modules -> The green "+" -> Scala -> Create... -> Download... -> 2.12.6... -> OK
* In IntelliJ IDEA: File -> Project Structure -> Modules -> Dependencies -> Under "Export" check scala-sdk-2.12.6
* In IntelliJ IDEA: Build -> Build Artifacts... -> Build

This will produce PurpleWave.jar. See below for "How to run PurpleWave"

## How to run PurpleWave
* From IntelliJ IDEA: Run -> Run 'PurpleWave' or Debug 'PurpleWave'
* As a JAR:
  - `cd` to the StarCraft directory
  - `mkdir -p bwapi-data/AI; mkdir -p bwapi-data/read; mkdir -p bwapi-data/write` to create the standard directories for BWAPI bot data
  - Copy PurpleWave.jar to `bwapi-data/AI`
  - `java.exe -jar bwapi-data/AI/PurpleWave.jar` <-- Run this from the StarCraft directory  

## Questions and feedback
Say hi! Post an issue here on Github or email dsgant at gmail

## License
PurpleWave is published under the MIT License. I encourage you to use PurpleWave as a starting point for your own creation!
