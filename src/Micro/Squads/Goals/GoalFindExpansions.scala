package Micro.Squads.Goals

import Information.Geography.Types.Base
import Lifecycle.With
import Mathematics.PurpleMath

class GoalFindExpansions extends GoalAssignToBases {
  
  override def toString: String = "Find enemy expansions"
  
  override def acceptsHelp: Boolean = squad.units.size < PurpleMath.clamp(With.self.supplyUsed / 80, 1, With.geography.neutralBases.size)
  
  override def peekNextBase: Base = With.intelligence.peekNextBaseToScout
  override def takeNextBase: Base = With.intelligence.dequeueNextBaseToScout
}