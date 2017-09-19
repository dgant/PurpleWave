package Information.Battles.Types

import Information.Geography.Types.Base
import Lifecycle.With
import Mathematics.Points.Pixel
import Mathematics.PurpleMath
import ProxyBwapi.UnitInfo.UnitInfo
import Utilities.ByOption

class Analysis(battle: Battle) {
  def nearestBase(bases: Iterable[Base]): Option[Pixel] = ByOption.minBy(bases.map(_.heart.pixelCenter))(_.groundPixels(battle.focus))
  val nearestBaseOurs       = nearestBase(With.geography.ourBases).getOrElse(With.geography.home.pixelCenter)
  val nearestBaseEnemy      = nearestBase(With.geography.enemyBases).getOrElse(With.intelligence.mostBaselikeEnemyTile.pixelCenter)
  val cannons               = nearestBaseOurs.zone.units.filter(cannon => cannon.canAttack && ! cannon.canMove && cannon.matchups.targets.nonEmpty && cannon.matchups.threatsInRange.nonEmpty)
  val urgencyOurs           = battle.focus.pixelDistanceFast(nearestBaseEnemy)
  val urgencyEnemy          = battle.focus.pixelDistanceFast(nearestBaseOurs)
  val urgencyRatio          = Math.sqrt(urgencyOurs / urgencyEnemy)
  val turtlingRatio         = Math.pow(0.97, cannons.size)
  val economyRatio          = With.geography.ourBases.size.toDouble / With.geography.enemyBases.size
  val chokeBadness          = getChokeBadness
  val flexibilityOurs       = meanFlexibility(battle.us.units)
  val flexibilityEnemy      = meanFlexibility(battle.enemy.units)
  val flexibilityRatio      = flexibilityOurs / flexibilityEnemy
  val hysteresisVoters      = battle.us.units.filter(unit     => unit.canMove && unit.friendly.exists(_.agent.canFight))
  val hysteresisEngaged     = hysteresisVoters.filter(_.friendly.exists(_.agent.shouldEngage))
  val hysteresisRatio       = if (hysteresisVoters.isEmpty) 0.0 else hysteresisEngaged.size.toDouble / hysteresisVoters.size
  val hysteresis            = 2.0 * hysteresisRatio
  val hysteresisDesireMin   = PurpleMath.clamp(       flexibilityRatio, 0.5, 0.7)
  val hysteresisDesireMax   = PurpleMath.clamp(1.0 /  flexibilityRatio, 1.1, 1.8)
  val desireTurtling        = PurpleMath.clamp(turtlingRatio, 0.8,  1.0)
  val desireUrgency         = PurpleMath.clamp(urgencyRatio,  0.8,  1.8)
  val desireChokiness       = PurpleMath.clamp(chokeBadness,  0.6,  1.0)
  val desireEconomy         = PurpleMath.clamp(economyRatio,  0.9,  1.3)
  val desireHysteresis      = PurpleMath.clamp(hysteresis,    hysteresisDesireMin,  hysteresisDesireMax)
  val desireMultiplier      = With.blackboard.aggressionRatio * desireTurtling * desireUrgency * desireChokiness * desireEconomy * desireHysteresis
  val attackGains           = battle.estimationSimulationAttack.costToEnemy
  val attackLosses          = battle.estimationSimulationAttack.costToUs
  val retreatGains          = battle.estimationSimulationRetreat.costToEnemy
  val retreatLosses         = battle.estimationSimulationRetreat.costToUs
  val desireTotal           = attackGains + retreatLosses / With.configuration.retreatCaution - retreatGains - attackLosses / desireMultiplier
  
  private def getChokeBadness: Double = {
    val zoneUs    = battle.us.centroid.zone
    val zoneEnemy = battle.enemy.centroid.zone
    if (zoneUs == zoneEnemy) return 1.0
    
    val edge      = zoneUs.edges.find(_.zones.contains(zoneEnemy))
    val edgeWidth = Math.min(32.0 * 2.0, edge.map(_.radiusPixels * 2.0).getOrElse(32.0 * 3.0))
    val ourWidth  = battle.us.units.filterNot(_.flying).map(unit => if (unit.flying) 0.0 else 2.0 * unit.unitClass.radialHypotenuse).sum
    PurpleMath.nanToOne(1.5 * edgeWidth / ourWidth)
  }
  
  private def meanFlexibility(units: Seq[UnitInfo]): Double = {
    val fighters          = units.filter(_.damageOnHitMax > 0)
    val totalFlexibility  = fighters.map(fighter => flexibility(fighter) * fighter.subjectiveValue).sum
    val denominator       = Math.max(fighters.map(_.subjectiveValue).sum, 1.0)
    totalFlexibility / denominator
  }
    
  private def flexibility(unit: UnitInfo): Double = {
    val rangeFlexibility = unit.pixelRangeMax
    val speedFlexibility = unit.topSpeedChasing * 24.0 * (if (unit.flying) 2.0 else 1.0)
    rangeFlexibility + speedFlexibility
  }
}
