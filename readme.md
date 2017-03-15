# PurpleWave
### A Scala framework for building AI players for *Starcraft: Brood War*

## About
Since 2008, many *Starcraft* and AI enthusiasts have been writing bots to play *Starcraft* using BWAPI (Brood War API).

PurpleWave is a framework for writing such bots.

Most popular Brood War AI libraries are written in C++, which provides a higher ceiling on performance, but which is less accessible to new developers and can be difficult to write expressively.

In order to iterate quickly on PurpleWave, and to leverage the JVM-based BWMirror library, I wrote PurpleWave in Scala.

## How PurpleWave works

PurpleWave combines a few popular techniques for AI development in a modular framework. You can assemble a strategy with mostly existing pieces combined with your own custom modules.

Some of the techniques PurpleWave uses are:
* Hierarchical task planning
* Priority-based resource allocation
* Build order simulation
* Agent-based behaviors
* Potential field-based navigation
* Dynamic CPU performance management

PurpleWave is built directly on BWAPI, but is designed to be easily adapted for Starcraft 2 AI development.

## Building on top of PurpleWave

PurpleWave is in early stage development and is thus changing rapidly. As such, it may be difficult to build a different bot based on it right now. As the API stabilizes, I'll provide more information.