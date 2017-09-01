package Information.Battles.Types

import Lifecycle.With
import Mathematics.PurpleMath
import ProxyBwapi.UnitInfo.UnitInfo

class Analysis(battle: Battle) {
  
  val nearestBaseOurs       = if (With.geography.ourBases.isEmpty)    With.geography.home.pixelCenter                     else With.geography.ourBases  .map(_.heart.pixelCenter).minBy(_.groundPixels(battle.focus))
  val nearestBaseEnemy      = if (With.geography.enemyBases.isEmpty)  With.intelligence.mostBaselikeEnemyTile.pixelCenter else With.geography.enemyBases.map(_.heart.pixelCenter).minBy(_.groundPixels(battle.focus))
  val urgencyOurs           = battle.focus.pixelDistanceFast(nearestBaseEnemy)
  val urgencyEnemy          = battle.focus.pixelDistanceFast(nearestBaseOurs)
  val urgencyRatio          = urgencyOurs / urgencyEnemy
  val economyRatio          = With.geography.ourBases.size.toDouble / With.geography.enemyBases.size
  val chokiness             = if (battle.us.centroid.zone == battle.enemy.centroid.zone) 1.0 else 0.0
  val flexibilityOurs       = meanFlexibility(battle.us.units)
  val flexibilityEnemy      = meanFlexibility(battle.enemy.units)
  val flexibilityRatio      = flexibilityOurs / flexibilityEnemy
  val hysteresisVoters      = battle.us.units.filter(unit     => unit.canMove && unit.friendly.exists(_.agent.canFight))
  val hysteresisEngaged     = hysteresisVoters.filter(_.friendly.exists(_.agent.shouldEngage))
  val hysteresisRatio       = if (hysteresisVoters.isEmpty) 0.0 else hysteresisEngaged.size.toDouble / hysteresisVoters.size
  val hysteresis            = 2.0 * hysteresisRatio
  val hysteresisDesireMin   = PurpleMath.clamp(       flexibilityRatio, 0.5, 0.9)
  val hysteresisDesireMax   = PurpleMath.clamp(1.0 /  flexibilityRatio, 1.1, 2.0)
  val desireUrgency         = PurpleMath.clamp(urgencyRatio,      0.8,                  1.8)
  val desireChokiness       = PurpleMath.clamp(chokiness,         0.6,                  1.1)
  val desireEconomy         = PurpleMath.clamp(economyRatio,      1.0,                  1.3)
  val desireHysteresis      = PurpleMath.clamp(hysteresis,        hysteresisDesireMin,  hysteresisDesireMax)
  val desireMultiplier      = With.blackboard.aggressionRatio * desireUrgency * desireChokiness * desireEconomy * desireHysteresis
  val attackGains           = battle.estimationSimulationAttack.costToEnemy
  val attackLosses          = battle.estimationSimulationAttack.costToUs
  val retreatGains          = battle.estimationSimulationRetreat.costToEnemy
  val retreatLosses         = battle.estimationSimulationRetreat.costToUs
  val desire                = desireMultiplier * attackGains + retreatLosses - attackLosses - retreatGains
  
  private def meanFlexibility(units: Seq[UnitInfo]): Double = {
    val fighters = units.filter(_.damageOnHitMax > 0)
    val totalFlexibility = fighters.map(fighter => flexibility(fighter) * fighter.subjectiveValue).sum
    val denominator = Math.max(fighters.map(_.subjectiveValue).sum, 1.0)
    totalFlexibility / denominator
  }
    
    private def flexibility(unit: UnitInfo): Double = {
    val rangeFlexibility = unit.pixelRangeMax
    val speedFlexibility = unit.topSpeedChasing * 24.0 * (if (unit.flying) 2.0 else 1.0)
    Math.max(rangeFlexibility, speedFlexibility)
  }
}
