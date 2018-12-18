package Micro.Squads.Goals

import Information.Geography.Types.Base
import Information.Intelligenze.BaseFilterExpansions
import Lifecycle.With
import Mathematics.PurpleMath
import Performance.Cache
import Planning.UnitMatchers.UnitMatchAntiGround
import ProxyBwapi.UnitInfo.FriendlyUnitInfo
import Utilities.ByOption

class GoalCampExpansions extends GoalAssignToBases {
  
  override def toString: String = "Camp " + likelyBasesCache().take(PurpleMath.clamp(squad.previousUnits.size, 1, 2)).mkString(", ")
  
  override def acceptsHelp: Boolean = squad.units.size < PurpleMath.clamp(With.geography.neutralBases.size, 0, 2)
  override def baseFilter: Base => Boolean = BaseFilterExpansions.apply

  private def likelyBasesCache = new Cache(() => {
    var candidateBases = With.geography.neutralBases.filter(_.mineralsLeft > 1000)
    if (candidateBases.isEmpty) candidateBases = With.geography.neutralBases
    if (candidateBases.isEmpty) candidateBases = With.intelligence.mostIntriguingBases()

    val distancesTheirs = candidateBases.map(base => (
        base,
        ByOption
          .min(With.geography.enemyBases.map(enemyBase => enemyBase.heart.groundPixels(base.heart) + enemyBase.heart.tileDistanceFast(base.heart)))
          .getOrElse(With.intelligence.mostBaselikeEnemyTile.tileDistanceFast(base.heart))))
      .toMap

    candidateBases.sortBy(base => distancesTheirs(base))
  })
  
  unitMatcher = UnitMatchAntiGround
  
  override def peekNextBase: Base = likelyBasesCache()(squad.units.size % likelyBasesCache().size)
  override def takeNextBase(unit: FriendlyUnitInfo): Base = peekNextBase
}
