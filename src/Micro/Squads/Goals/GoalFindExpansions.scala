package Micro.Squads.Goals

import Information.Geography.Types.Base
import Information.Intelligenze.BaseFilterExpansions
import Lifecycle.With
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

class GoalFindExpansions extends GoalAssignToBases {
  
  override def toString: String = "Scout expansions: " + destination.base.map(_.toString).getOrElse("(No base)")

  override def baseFilter: Base => Boolean = BaseFilterExpansions.apply
  override def takeNextBase(scout: FriendlyUnitInfo): Base = With.intelligence.claimBaseToScoutForUnit(scout, BaseFilterExpansions.apply)

  // TODO
  ???
}
