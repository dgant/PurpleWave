package Micro.Squads.Goals

import Information.Geography.Types.Base
import Lifecycle.With
import Mathematics.PurpleMath
import Performance.Cache
import Planning.UnitMatchers.UnitMatchAntiGround
import Utilities.ByOption

class GoalCampExpansions extends GoalAssignToBases {
  
  override def toString: String = "Camp enemy expansions"
  
  override def acceptsHelp: Boolean = squad.units.size < PurpleMath.clamp(With.self.supplyUsed / 80, 1, With.geography.neutralBases.size)
  
  private def likelyBasesCache = new Cache(() => {
    val candidateBases = With.geography.neutralBases.filter(_.mineralsLeft > 1000).toVector
    val distancesTheirs = candidateBases.map(base => (
        base,
        ByOption
          .min(With.geography.enemyBases.map(enemyBase => enemyBase.heart.groundPixels(base.heart) + enemyBase.heart.tileDistanceFast(base.heart)))
          .getOrElse(With.intelligence.mostBaselikeEnemyTile.tileDistanceFast(base.heart))))
      .toMap
    val distancesOurs = candidateBases.map(base => (
      base,
      ByOption
        .min(With.geography.ourBases.map(ourBase => ourBase.heart.groundPixels(base.heart) + ourBase.heart.tileDistanceFast(base.heart)))
        .getOrElse(With.geography.home.tileDistanceFast(base.heart))))
      .toMap
    var output = candidateBases.sortBy(base => distancesTheirs(base) / (1.0 + distancesOurs(base)))
    if (output.isEmpty) output = With.geography.neutralBases.toVector
    if (output.isEmpty) output = With.intelligence.leastScoutedBases.toVector
    output
  })
  
  unitMatcher = UnitMatchAntiGround
  
  override def peekNextBase: Base = takeNextBase
  override def takeNextBase: Base = likelyBasesCache()(squad.units.size % likelyBasesCache().size)
}
