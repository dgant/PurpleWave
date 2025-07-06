# PurpleWave
### A tournament-winning AI player of *StarCraft: Brood War*

## About
[PurpleWave](https://github.com/dgant/PurpleWave) is a *StarCraft: Brood War* AI written in Scala. It can play all three races and a large variety of professional-style strategies.

[![BASIL](https://img.shields.io/endpoint?url=https%3A//basil-badge-production.up.railway.app/badge/PurpleWave)](https://www.basil-ladder.net/ranking.html) as Protoss <br/>  

[![BASIL](https://img.shields.io/endpoint?url=https%3A//basil-badge-production.up.railway.app/badge/PurpleSpirit)](https://www.basil-ladder.net/ranking.html) offracing as Terran <br/>  

[![BASIL](https://img.shields.io/endpoint?url=https%3A//basil-badge-production.up.railway.app/badge/PurpleSwarm)](https://www.basil-ladder.net/ranking.html) offracing as Zerg <br/>

PurpleWave has won:
 * :trophy: 1st Place in the [2019-20 SSCAIT](https://sscaitournament.com/index.php?action=2019)
 * :trophy: 1st Place in the [2019 IEEE CoG StarCraft AI Competition](https://cilab.gist.ac.kr/sc_competition2019/?cat=17)
 * :trophy: 1st Place in the [2018-19 SSCAIT](https://sscaitournament.com/index.php?action=2018)
 * :trophy: 1st Place in the [2018 AIST S1](https://sites.google.com/view/aistarcrafttournament/aist-s1)
 * :2nd_place_medal: 2nd Place in the [2024 AIIDE StarCraft AI Competition](https://www.cs.mun.ca/~dchurchill/starcraftaicomp/2024/)
 * :2nd_place_medal: 2nd Place in the [2023 IEEE COG StarCraft AI Competition](https://cilab.gist.ac.kr/sc_competition/?cat=17)
 * :2nd_place_medal: 2nd Place in the [2022 IEEE COG StarCraft AI Competition](https://cilab.gist.ac.kr/sc_competition2022/?cat=17)
 * :2nd_place_medal: 2nd Place in the [2021 AIST S4](https://sites.google.com/view/aistarcrafttournament/aist-s4)
 * :2nd_place_medal: 2nd Place in the [2020 AIIDE StarCraft AI Competition](https://www.cs.mun.ca/~dchurchill/starcraftaicomp/2020/)
 * :2nd_place_medal: 2nd Place in the [2020 IEEE COG StarCraft AI Competition](https://cilab.gist.ac.kr/sc_competition/?p=1162)
 * :2nd_place_medal: 2nd Place in the [2020 AIST S3](https://sites.google.com/view/aistarcrafttournament/aist-s3)
 * :2nd_place_medal: 2nd Place in the [2019 AIIDE StarCraft AI Competition](https://www.cs.mun.ca/~dchurchill/starcraftaicomp/2019/)
 * :2nd_place_medal: 2nd Place in the [2017 AIIDE StarCraft AI Competition](https://www.cs.mun.ca/~dchurchill/starcraftaicomp/2017/)
 * :2nd_place_medal: 2nd Place in the [2018 IEEE CIG StarCraft AI Competition](https://cilab.gist.ac.kr/sc_competition2018/?cat=17)
 * :3rd_place_medal: 3rd Place in the [2023 AIIDE StarCraft AI Competition](https://www.cs.mun.ca/~dchurchill/starcraftaicomp/2023/)
 * :3rd_place_medal: 3rd Place in the [2022-23 SSCAIT](https://sscaitournament.com/index.php?action=2022)
 * :3rd_place_medal: 3rd Place in the [2017 IEEE CIG StarCraft AI Competition](https://cilab.gist.ac.kr/sc_competition2017/?cat=17)
  
PurpleWave has also ranked #1 on the [BASIL](https://basil.bytekeeper.org/ranking.html), SSCAIT, and [SAIL](https://www.cs.mun.ca/~z24rmk/starcraftailaddertest/about) ladders.

## Credits
Thanks to:
* Nathan Roth (Antiga/Iruian) for strategy advice and consulting -- so much of the polish in PurpleWave's strategies comes from his wisdom and replay analysis
* @JasperGeurtz @Bytekeeper and @N00byEdge for [JBWAPI](https://github.com/JavaBWAPI/JBWAPI)
* @vjurenka for [BWMirror](https://github.com/vjurenka/BWMirror)
* @Cmccrave for [Horizon](https://github.com/Cmccrave/Horizon) and [BWEB](https://github.com/Cmccrave/BWEB)
* @MrTate for [JBWEB](https://github.com/MrTate/JBWEB)
* @lowerlogic for [BWTA](https://code.google.com/archive/p/bwta/)
* Igor Dimitrijevic for [BWEM](http://bwem.sourceforge.net/)
* @JasperGeurtz for the [Java port of BWEM](https://github.com/JavaBWAPI/JBWAPI/tree/develop/src/main/java/bwem)
* @kovarex and @heinermann for [BWAPI](https://github.com/bwapi/bwapi)
* @michalsustr and @certicky for [SC-Docker](https://github.com/Games-and-Simulations/sc-docker) and @Bytekeeper for its [BASIL port](https://github.com/basil-ladder/sc-docker/) for powering [PotatoPeeler](https://github.com/dgant/PotatoPeeler)
* @jabbo16 for configuring PurpleWave's Maven build
* @davechurchill @certicky @krasi0 @Bytekeeper @bgweber @SonkoMagnus Nathan Roth and the Cognition & Intelligence Lab at Sejong University for hosting Brood War competitions and environments that have given PurpleWave visibility and purpose
* @chriscoxe for diagnosing and solving technical issues in tournament environments that have affected PurpleWave's ability to compete
* @jaj22/JohnJ and Ankmairdor for lots of advice navigating Brood War mechanics
* @IMP42 @AdakiteSystems and @tscmoo for helping me get BWAPI up and running when I was getting started
* @tscmoo for [OpenBW](https://github.com/OpenBW/openbw/)
* NepetaNigra, ChoboSwaggins, @Nitekat, and CH Miner for sharing PurpleWave's games with the world and helping tell our story 

The community around BWAPI and StarCraft AI is amazing and PurpleWave could not exist without building on the decade of work these folks have done.

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
