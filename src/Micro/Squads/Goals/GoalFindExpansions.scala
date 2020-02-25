package Micro.Squads.Goals

import Information.Geography.Types.Base
import Information.Intelligenze.BaseFilterExpansions
import Lifecycle.With
import Mathematics.PurpleMath
import Planning.UnitCounters.UnitCountUpToLambda
import Planning.UnitMatchers.{UnitMatchMobile, UnitMatchNot, UnitMatcher}
import ProxyBwapi.Races.{Protoss, Terran}
import ProxyBwapi.UnitInfo.FriendlyUnitInfo
import Utilities.CountMap

class GoalFindExpansions extends GoalAssignToBases {

  override def inherentValue: Double = GoalValue.scout
  
  override def toString: String = "Scout expansions: " + destination.base.map(_.toString).getOrElse("(No base)")

  override def baseFilter: Base => Boolean = BaseFilterExpansions.apply
  override def takeNextBase(scout: FriendlyUnitInfo): Base = With.intelligence.claimBaseToScoutForUnit(scout, BaseFilterExpansions.apply)

  def scoutsWanted: Int = PurpleMath.clamp(With.self.supplyUsed / 80, 1, With.geography.neutralBases.size)

  override def qualityNeeds: CountMap[UnitMatcher] = {
    val output = new CountMap[UnitMatcher]
    output(UnitMatchMobile) = scoutsWanted
    output
  }

  unitCounter = new UnitCountUpToLambda(() => scoutsWanted)
  unitMatcher = UnitMatchNot(unit => unit.topSpeed < Protoss.Zealot.topSpeed || unit.isAny(Terran.Battlecruiser, Terran.Valkyrie, Terran.Dropship, Protoss.Carrier, Protoss.Shuttle))
}
