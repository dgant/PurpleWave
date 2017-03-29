# Battle Simulation

## Purpose
Battle simulators usually try to answer the question *"Who will win?"*
But that's not actually the most important question, because it doesn't produce actionable decisions.

Instead, we care about questions like:
+ "Should we fight?"
+ "How should we fight?"
+ "Who should fight?"
+ "How do the current fights impact our decisionmaking?"

So we want to look at the potential fights and provide information that will
help us make decisions, and help agents decide what to do.

##### Should we fight?
+ Should units pursue the enemy?
+ Should they ignore them?
+ Should they flee?
+ Will we be at a disadvantage if we wait to fight?

##### How should we fight?
Look at each unit's role in a battle and decide what role(s) it needs to fulfill.
+ Can we afford to let an injured unit flee?
+ Should some units soak up damage?
+ Should we hold a chokepoint?
+ Who should we focus fire?

##### Who should fight?
+ Do we need to pull workers?
+ Do we need to spend our casters' energy?

##### How do the current fights impact our decisionmaking?
+ Can we expand?
+ Do we need to pump out units?
+ Can we tech up?

## Structure

##### Identify what battles are happening
There are any number of ways to define a "battle". The goal is to help answer specific questions, so we look at battles from various areas of concern 
We identify battles by areas of concern: 
+ **Micromanagement:** Battles happening between ad-hoc clusters of units
+ **Macromanagement:** Battles happening in bases, with economic implications
+ **Planning:** The overall battle between our army and their army
 
