package Information.Battles.Types

import Lifecycle.With
import Mathematics.PurpleMath
import ProxyBwapi.UnitInfo.UnitInfo

class Analysis(battle: Battle) {
  val nearestBaseOurs   = if (With.geography.ourBases.isEmpty)    With.geography.home.pixelCenter                     else With.geography.ourBases  .map(_.heart.pixelCenter).minBy(_.groundPixels(battle.focus))
  val nearestBaseEnemy  = if (With.geography.enemyBases.isEmpty)  With.intelligence.mostBaselikeEnemyTile.pixelCenter else With.geography.enemyBases.map(_.heart.pixelCenter).minBy(_.groundPixels(battle.focus))
  val urgencyOurs       = battle.focus.pixelDistanceFast(nearestBaseEnemy)
  val urgencyEnemy      = battle.focus.pixelDistanceFast(nearestBaseOurs)
  val fighters          = battle.us.units.filter(_.canAttack)
  val aggressionDesire  = With.blackboard.aggressionRatio
  val flexibilityOurs   = meanFlexibility(battle.us.units)
  val flexibilityEnemy  = meanFlexibility(battle.enemy.units)
  val flexibilityRatio  = flexibilityOurs / flexibilityEnemy
  val urgencyRatio      = urgencyOurs / urgencyEnemy
  val chokiness         = if (battle.us.centroid.zone == battle.enemy.centroid.zone) 1.0 else 0.0
  val economyRatio      = With.geography.ourBases.size.toDouble / With.geography.enemyBases.size
  val hysteresisVoters  = battle.us.units.filter(unit => unit.canMove && unit.friendly.exists(_.agent.canFight))
  val hysteresisEngaged = hysteresisVoters.filter(_.friendly.exists(_.agent.shouldEngage))
  val hysteresisRatio   = if (hysteresisVoters.isEmpty) 0.0 else hysteresisEngaged.size.toDouble / hysteresisVoters.size
  val hysteresis        = 2.0 * hysteresisRatio
  val flexibilityDesire = PurpleMath.clamp(flexibilityRatio,  0.9, 1.3)
  val urgencyDesire     = PurpleMath.clamp(urgencyRatio,      0.8, 1.8)
  val chokinessDesire   = PurpleMath.clamp(chokiness,         0.6, 1.1)
  val economyDesire     = PurpleMath.clamp(economyRatio,      0.9, 1.3)
  val hysteresisDesire  = PurpleMath.clamp(hysteresis,        0.9, 1.2)
  val bonusDesire       = aggressionDesire * flexibilityDesire * urgencyDesire * chokinessDesire * economyDesire * hysteresisDesire
  val estimationAttack  = battle.estimationSimulationAttack
  val estimationRetreat = battle.estimationSimulationRetreat
  val attackGains       = estimationAttack.costToEnemy  + estimationRetreat.costToUs
  val attackLosses      = estimationAttack.costToUs     + estimationRetreat.costToEnemy
  val desire            = bonusDesire * attackGains - attackLosses
  
  private def meanFlexibility(units: Seq[UnitInfo]): Double = {
    val fighters = units.filter(_.damageOnHitMax > 0)
    val totalFlexibility = fighters.map(fighter => flexibility(fighter) * fighter.subjectiveValue).sum
    val denominator = Math.max(fighters.map(_.subjectiveValue).sum, 1.0)
    totalFlexibility / denominator
  }
    
    private def flexibility(unit: UnitInfo): Double = {
    val rangeFlexibility = unit.pixelRangeMax
    val speedFlexibility = unit.topSpeed * 24.0 * (if (unit.flying) 2.0 else 1.0)
    Math.max(rangeFlexibility, speedFlexibility)
  }
}
