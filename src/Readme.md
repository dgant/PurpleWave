# Overview 

## Starcraft packages 

##### Information

Organizes information about the current state of the game.
* Geography (Where can I build a base?)
* Economy (What's my mineral income?)
* Grids (Which tiles are walkable?)
* Battles (Which units are fighting which other units? What would happen if we fought?)
* Intelligence (What can we deduce about our opponent?)

##### Planning

This is where we actually make decisions about the game. Decisions are structured roughly as a https://en.wikipedia.org/wiki/Hierarchical_task_network
 
The strategy is specified as a tree of goals ("Plans"). A Plan may have a series of sub-goals, which it may attempt to fulfill one-at-a-time or all at the same time.

A potential problem is that two plans might want to use the same unit, spend the same minerals, or build in the same area. To resolve that -- and to ensure that important plans are never interrupted by less-important plans -- all resources are only made available to Plans via mutex locks. Plans request access to various resources (units, minerals, gas, supply, or tiles on the map). Plans have an implicit priority based on their structure in the goal tree. Higher priority plans get first dibs on the resources they need, while lower-priority plans may have to wait.

Plans specify the resources they need as generally as possible. Instead of saying, "give me that unit" a Plan would request "give me the 5 nearest units that can repair" or "give me everyone who can attack".

##### Macro

The Planning package makes decisions about what we want to do. The Macro package decides how to best serve those decisions by allocating resources and ordering builds.

* The Allocation package assigns resources to Plans based on priority and preferences.
* The Scheduling package optimizes the construction and spending needs of Plans to avoid wasting time or money.
* The Architect finds places to put buildings

##### Micro

The Planning package makes decisions about what goals unit should try to achieve. The Micro package determines how units should try to achieve them.
 
When a Plan wants a unit to achieve a goal, it gives that unit an Intention. That Intention might say "go attack the enemy's base" or "mine these minerals" or "block this ramp". The Intention is a very loose statement of purpose which, executed literally, would lead to fairly naive behavior.

The Micro package empowers each unit to intelligently pursue the goal it's been given.
* The Behavior package chooses the consensus best action for each unit.
* The Commander directly executes those actions using BWAPI, while navigating Starcraft's internal engine quirks. 

## Technical packages

##### ProxyBwapi

BWAPI calls through BWMirror have high overhead. ProxyBwapi is a caching layer to minimize that overhead. ProxyBwapi also contains logic for interpreting that data in the context of Brood War engine rules.

##### Lifecycle

* Application startup
* Connecting to BWAPI
* Handlers for BWAPI events
* Instantiation and cleanup of global state

##### Performance

Starcraft is a fast-paced game and PurpleWave does a ton of real-time calculations. The Performance package contains tools for measuring and optimizing the performance of those calculations to ensure that PurpleWave can run at an acceptable speed.

##### Debugging

Testing and debugging in a real-time game environment is difficult. The Debugging package has a lot of tools for understanding what PurpleWave is doing underneath the hood.

The biggest piece of this package is the Visualization package, which graphically represents PurpleWave's internal state. 

##### Mathematics
##### Utilities

Generally applicable logic that's used across the codebase.