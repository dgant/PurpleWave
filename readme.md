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

## License

PurpleWave is published under the MIT License.