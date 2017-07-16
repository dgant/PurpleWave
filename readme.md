# PurpleWave
### An extensible AI player for *Starcraft: Brood War*

## About
Since 2008, many *Starcraft* and AI enthusiasts have been writing bots to play *Starcraft* using BWAPI (Brood War API).

PurpleWave is such a bot, and provides a framework for writing more such bots.

Most popular Brood War AI libraries are written in C++, which provides a higher ceiling on performance, but which is less accessible to new developers and can be difficult to write expressively.

In order to iterate quickly on PurpleWave, and to leverage the JVM-based BWMirror library, I wrote PurpleWave in Scala.

## How PurpleWave works

PurpleWave combines a few popular techniques for AI development in a modular framework. You can assemble a strategy with mostly existing pieces combined with your own custom modules.

Some of the techniques PurpleWave uses are:
* Hierarchical task planning
* Priority-based resource allocation
* Build order simulation
* Agent-based behaviors
* Heuristic targeting and navigation with potential fields
* Dynamic CPU performance management

PurpleWave is built directly on BWAPI, but is designed to be easily adapted for Starcraft 2 AI development.

## How to build & run PurpleWave

What you'll need:
* Windows
* 32-bit (x86) Java Runtime Environment 8: http://www.oracle.com/technetwork/java/javase/downloads/jre8-downloads-2133155.html
* 32-bit (x86) Java Development Kit 8:http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html
* IntelliJ IDEA Community Edition: https://www.jetbrains.com/idea/download/#section=windows
* (Optional) Git: Git-2.13.2-64-bit.exe
* (Very optional, for packaging PurpleWave as an executable) Launch4j: http://launch4j.sourceforge.net/

Steps: 
* Clone or download this repository (to c:\prog\PurpleWave1 may be easiest; others may require changing some directories)
* Open IntelliJ IDEA
* In IntelliJ IDEA: File -> Plugins -> Scala
* In IntelliJ IDEA: File -> Open -> Select the directory where you cloned/downloaded PurpleWave
* In IntelliJ IDEA: File ->  Project Structure -> Select the Java Development Kit directory (like c:\Program Files\Java\jdk\1.8.0_121)
* In IntelliJ IDEA: Build -> Build Artifacts... -> Build

This will produce PurpleWave.jar. From the top-level PurpleWave directory, and with Java.exe on your system path, you can run this with 

If you'd like to run or distribute PurpleWave as a standalone executable, here's how:
* Run Launch4j (see link above for download)
* In Launch4j: Open -> From the PurpleWave directory, launch4j/PurpleWave-launch4j.xml
* In Launch4j: Point the Jar to the Jar you just built.
* In Launch4j: Double-check the Output file and Jar directories
* In Launch4j: Click build
* Run PurpleWave.exe

You should find PurpleWave.exe in a directory with several DLLs which it needs to run:
* BWAPI.dll
* bwapi_bridge2_5.dll
* libgmp-10.dll
* libmpfr-4.dll

## Running PurpleWave
* From IntelliJ IDEA: Run -> Run 'PurpleWave' or Debug 'PurpleWave'
* As a JAR: java.exe -jar ./out/artifacts/PurpleWave.jar 
* As an EXE: Just double-click it! Make sure if you move the EXE that you move the aforementioned DLLs along with it

## Questions and feedback
Say hi! Post an issue here on Github or email d x s x g x a x n x t @gmail.com (without the x's)

## License
PurpleWave is published under the MIT License.