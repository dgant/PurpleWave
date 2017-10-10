# PurpleWave
### An extensible AI player for *StarCraft: Brood War*

## About
PurpleWave is a *StarCraft: Brood War* AI written in Scala. It can play all races and a large variety of strategies.

PurpleWave won 3rd Place in the [2017 IEEE CIG Starcraft Tournament](https://cilab.sejong.ac.kr/sc_competition/?p=1090). It's been ranked as high as #2 on the [SSCAIT Ladder](https://sscaitournament.com/index.php?action=scores). 

## Credits
Thanks to:
* Nathan Roth (Antiga/Iruian) for strategy advice and consulting -- so much of the polish in PurpleWave's strategies comes from his wisdom and replay analysis
* @jaj22/JohnJ for lots of advice navigating Brood War mechanics
* @IMP42 @AdakiteSystems and @tscmoo for helping me get BWAPI up and running when I was getting started
* @kovarex @heinermann @bgweber @certicky @krasi0 @davechurchill and the Cognition & Intelligence Lab at Sejong University for making Brood War competitions possible in the first place
* @vjurenka for BWMirror and Luke Perkins for BWTA

## How to build PurpleWave
What you'll need:
* Windows
* BWMirror 2.5, via https://github.com/vjurenka/BWMirror/blob/master/dist/bwmirror_v2_5.jar 
* 32-bit (x86) Java Runtime Environment 8: http://www.oracle.com/technetwork/java/javase/downloads/jre8-downloads-2133155.html
* 32-bit (x86) Java Development Kit 8:http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html
* IntelliJ IDEA Community Edition: https://www.jetbrains.com/idea/download/#section=windows
* (Optional) Git: Git-2.13.2-64-bit.exe
* (Very optional, if you'd like to package PurpleWave as an EXE) Launch4j: http://launch4j.sourceforge.net/

Steps: 
* Clone or download this repository (I keep it in c:\prog\pw but it should work from anywhere)
* Put bwmirror_v2_5.jar in /lib
* Make a copy of bwmirror_v2_5.jar called bwmirror_v2_5.zip
* From bwmirror_v2_5.zip, extract bwapi_bridge2_5.dll, libgmp-10.dll, and libmpfr-4.dll to /lib
* Open IntelliJ IDEA
* In IntelliJ IDEA: File -> Settings -> Plugins -> Check off Scala
* In IntelliJ IDEA: File -> Open -> Select the PurpleWave directory
* In IntelliJ IDEA: File -> Project Structure -> Select the Java Development Kit directory (like c:\Program Files\Java\jdk\1.8.0_121)
* In IntelliJ IDEA: Build -> Build Artifacts... -> Build

This will produce PurpleWave.jar. See below for "How to run PurpleWave"

(Optional) If you'd like to run or distribute PurpleWave as a standalone executable, here's how:
* Run Launch4j (see link above for download)
* In Launch4j: Open -> From the PurpleWave directory, launch4j/PurpleWave-launch4j.xml
* In Launch4j: Point the Jar to the Jar you just built.
* In Launch4j: Double-check the Output file and Jar directories
* In Launch4j: Click build. This will producePurpleWave.exe

You should find PurpleWave.exe in a directory with several DLLs which it needs to run:
* BWAPI.dll
* bwapi_bridge2_5.dll
* libgmp-10.dll
* libmpfr-4.dll

## How to run PurpleWave
* From IntelliJ IDEA: Run -> Run 'PurpleWave' or Debug 'PurpleWave'
* As a JAR: java.exe -jar ./out/artifacts/PurpleWave.jar <-- Make sure you use a 32-bit version of java.exe! 
* As an EXE: Just double-click it! Make sure if you move the EXE that you move the aforementioned DLLs along with it

## Questions and feedback
Say hi! Post an issue here on Github or email dsgant at gmail

## License
PurpleWave is published under the MIT License.