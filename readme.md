# PurpleWave
### An extensible AI player for *StarCraft: Brood War*

https://github.com/dgant/PurpleWave

## About
PurpleWave is a *StarCraft: Brood War* AI written in Scala. It can play all races and a large variety of strategies.

PurpleWave has won:
 * 1st Place in the [2018 AIST1](https://sites.google.com/view/aistarcrafttournament/aist-s1-2018)
 * 2nd Place in the [2017 AIIDE StarCraft AI Competition](http://www.cs.mun.ca/~dchurchill/starcraftaicomp/2017/)
 * 3rd Place in the [2017 IEEE CIG StarCraft AI Competition](https://cilab.sejong.ac.kr/sc_competition/?p=1090)
 * And has ranked as high as #2 on the [SSCAIT Ladder](https://sscaitournament.com/index.php?action=scores). 

PurpleWave vs. Iron, AIIDE 2017:

[![PurpleWave vs. Iron, AIIDE 2017](https://img.youtube.com/vi/g33PIqDdTqs/0.jpg)](https://www.youtube.com/watch?v=g33PIqDdTqs)

## Credits
Thanks to:
* Nathan Roth (Antiga/Iruian) for strategy advice and consulting -- so much of the polish in PurpleWave's strategies comes from his wisdom and replay analysis
* @jaj22/JohnJ for lots of advice navigating Brood War mechanics
* @IMP42 @AdakiteSystems and @tscmoo for helping me get BWAPI up and running when I was getting started
* @kovarex @heinermann @bgweber @certicky @krasi0 @davechurchill and the Cognition & Intelligence Lab at Sejong University for making Brood War competitions possible in the first place
* @vjurenka for BWMirror and Luke Perkins for BWTA

## How to build PurpleWave
[See build instructions in install.md](install.md)

Steps: 
* Clone or download this repository (I keep it in c:\p\pw but it should work from anywhere)
* Put bwmirror_v2_5.jar in /lib
* Make a copy of bwmirror_v2_5.jar called bwmirror_v2_5.zip
* From bwmirror_v2_5.zip, extract bwapi_bridge2_5.dll, libgmp-10.dll, and libmpfr-4.dll to /lib
* Open IntelliJ IDEA
* In IntelliJ IDEA: File -> Settings -> Plugins -> Check off Scala
* In IntelliJ IDEA: File -> Open -> Select the PurpleWave directory
* In IntelliJ IDEA: File -> Project Structure -> Select the Java Development Kit directory (like c:\Program Files\Java\jdk\1.8.0_121)
* In IntelliJ IDEA: File -> Project Structure -> Modules -> The green "+" -> Scala -> Create... -> Download... -> 2.12.6... -> OK
* In IntelliJ IDEA: File -> Project Structure -> Modules -> Dependencies -> Under "Export" check both bwmirror_v2_5 and scala-sdk-2.12.6
* In IntelliJ IDEA: Build -> Build Artifacts... -> Build

This will produce PurpleWave.jar. See below for "How to run PurpleWave"

You should find PurpleWave.exe in a directory with several DLLs which it needs to run:
* BWAPI.dll
* bwapi_bridge2_5.dll
* libgmp-10.dll
* libmpfr-4.dll

## How to run PurpleWave
* From IntelliJ IDEA: Run -> Run 'PurpleWave' or Debug 'PurpleWave'
* As a JAR: java.exe -jar ./out/artifacts/PurpleWave.jar <-- Make sure you use a 32-bit version of java.exe!

## Questions and feedback
Say hi! Post an issue here on Github or email dsgant at gmail

## License
PurpleWave is published under the MIT License. Make PurpleWave your own!