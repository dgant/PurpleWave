package Information.Battles.Types

import Information.Geography.Types.Base
import Lifecycle.With
import Mathematics.Points.Pixel
import Mathematics.PurpleMath
import ProxyBwapi.Races.{Protoss, Terran, Zerg}
import ProxyBwapi.UnitInfo.UnitInfo
import Utilities.ByOption

class SimulationParameters (battle: Battle) {
  
  def nearestBase(bases: Iterable[Base]): Option[Pixel] = ByOption.minBy(bases.map(_.heart.pixelCenter))(_.groundPixels(battle.focus))
  val nearestBaseOurs       = nearestBase(With.geography.ourBases).getOrElse(With.geography.home.pixelCenter)
  val nearestBaseEnemy      = nearestBase(With.geography.enemyBases).getOrElse(With.intelligence.mostBaselikeEnemyTile.pixelCenter)
  val lonelyCannons         = getLonelyCannons(nearestBaseOurs)
  val turtlingRatio         = Math.pow(0.97, lonelyCannons.size) // TODO: Consider ratio of cannons to army size
  val urgencyOurs           = battle.focus.pixelDistance(nearestBaseEnemy)
  val urgencyEnemy          = battle.focus.pixelDistance(nearestBaseOurs)
  val urgencyRatio          = Math.sqrt(urgencyOurs / urgencyEnemy)
  val chokeMobility         = getChokeMobility
  val flexibilityOurs       = meanFlexibility(battle.us.units)
  val flexibilityEnemy      = meanFlexibility(battle.enemy.units)
  val flexibilityRatio      = flexibilityOurs / flexibilityEnemy
  val hysteresisVoters      = battle.us.units.filter(_.canMove)
  val hysteresisVotingYes   = hysteresisVoters.filter(_.friendly.exists(_.agent.shouldEngage))
  val hysteresisRatio       = if (hysteresisVoters.isEmpty) 0.0 else hysteresisVotingYes.size.toDouble / hysteresisVoters.size
  val hysteresis            = 2.0 * hysteresisRatio
  val hysteresisDesireMin   = PurpleMath.clamp(       flexibilityRatio, 0.5, 0.7)
  val hysteresisDesireMax   = PurpleMath.clamp(1.0 /  flexibilityRatio, 1.1, 1.8)
  val desireTurtling        = PurpleMath.clamp(turtlingRatio, 0.7,  1.0)
  val desireUrgency         = PurpleMath.clamp(urgencyRatio,  0.9,  1.8)
  val desireChokiness       = PurpleMath.clamp(chokeMobility, 0.6,  1.0)
  val desireHysteresis      = PurpleMath.clamp(hysteresis,    hysteresisDesireMin,  hysteresisDesireMax)
  val desireMultiplier      = With.blackboard.aggressionRatio * desireTurtling * desireUrgency * desireChokiness * desireHysteresis
  
  def getLonelyCannons(nearestBaseOurs: Pixel): Set[UnitInfo] = {
    val candidates = nearestBaseOurs.zone.units ++ battle.us.units
    candidates
      .filter(unit =>
        (
          unit.unitClass.isStaticDefense
          || unit.is(Terran.SiegeTankSieged)
          || unit.is(Protoss.Reaver)
          || unit.is(Protoss.HighTemplar)
          || unit.is(Zerg.Lurker)
        )
        && unit.matchups.targetsInRange.isEmpty
        && unit.matchups.threatsInRange.isEmpty)
  }
  
  private def getChokeMobility: Double = {
    val zoneUs    = battle.us.centroid.zone
    val zoneEnemy = battle.enemy.centroid.zone
    if (zoneUs == zoneEnemy) return 1.0
    val crossers  = battle.us.units
    val edge      = zoneUs.edges.find(_.zones.contains(zoneEnemy))
    val edgeWidth = Math.max(32.0, edge.map(_.radiusPixels * 2.0).getOrElse(32.0 * 10.0))
    val ourWidth  = battle.us.units.filterNot(_.flying).map(unit => if (unit.flying) 0.0 else 2.0 * unit.unitClass.dimensionMax).sum
    PurpleMath.nanToOne(2.5 * edgeWidth / ourWidth)
  }
  
  private def meanFlexibility(units: Seq[UnitInfo]): Double = {
    val fighters          = units.filter(_.damageOnHitMax > 0)
    val totalFlexibility  = fighters.map(fighter => fighter.subjectiveValue * flexibility(fighter)).sum
    val denominator       = fighters.map(fighter => fighter.subjectiveValue).sum
    totalFlexibility / Math.max(1.0, denominator)
  }
  
  private def flexibility(unit: UnitInfo): Double = {
    val rangeFlexibility = unit.pixelRangeMax
    val speedFlexibility = unit.topSpeed * 36.0 * (if (unit.flying) 1.5 else 1.0)
    rangeFlexibility + speedFlexibility
  }
}
