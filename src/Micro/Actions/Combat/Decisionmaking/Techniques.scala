package Micro.Actions.Combat.Decisionmaking

import Debugging.SimpleString

object Techniques {
  abstract class Technique(val transitions: Technique*) extends SimpleString {
    def canTransition(other: Technique): Boolean = transitions.contains(other)
  }
  object Aim        extends Technique // Stand in place and shoot
  object Dodge      extends Technique // Heed pushes
  object Excuse     extends Technique(Dodge) // Let other units shove through us
  object Walk       extends Technique(Dodge, Excuse) // Just go on our merry way
  object Abuse      extends Technique(Dodge, Excuse) // Pick fights from range
  object Scavenge   extends Technique(Dodge, Excuse) // Eat around the edge of the fight
  object Fallback   extends Technique(Dodge) // Get out of fight while landing shots
  object Flee       extends Technique(Dodge, Abuse, Fallback) // Get out of fight
  object Fight      extends Technique(Dodge, Abuse, Excuse, Flee, Scavenge) // Pick fight ASAP

}
