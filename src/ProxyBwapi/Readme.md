# ProxyBwapi

## Motivation

BWAPI, as made accessible via BWMirror, has a few limitations:

* BWAPI only exposes currently available information. Units in the fog of war are inaccessible
* Native BWAPI calls through BWMirror have a substantial overhead. Even a simple call like Game.getFrameCount quickly becomes expensive
* The raw BWAPI type APIs can be confusing and are easily misused (for example, getPixel (unit center) vs getTile (unit corner))
* The raw BWAPI types aren't extensible

For those reasons, the rest of PurpleWave interacts with ProxyBwapi. ProxyBwapi provides a faster, safer interface for interacting with the game state, and tracks information on units in the fog of war. 

