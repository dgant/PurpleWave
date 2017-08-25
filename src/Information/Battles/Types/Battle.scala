package Information.Battles.Types

import Information.Battles.BattleUpdater
import Information.Battles.Estimations.Estimation
import Lifecycle.With
import Mathematics.Points.Pixel
import Mathematics.PurpleMath
import ProxyBwapi.UnitInfo.UnitInfo
import Utilities.EnrichPixel.EnrichedPixelCollection

class Battle(
  val us    : Team,
  val enemy : Team) {
  
  us.battle     = this
  enemy.battle  = this
  
  /////////////////
  // Estimations //
  /////////////////
  
  lazy val estimationAbstract           : Estimation  = BattleUpdater.estimateAvatar(this, geometric = false, weAttack = true,  enemyAttacks = true,  weRetreat = false)
  lazy val estimationAbstractOffense    : Estimation  = BattleUpdater.estimateAvatar(this, geometric = false, weAttack = true,  enemyAttacks = false, weRetreat = false)
  lazy val estimationSimulationAttack   : Estimation  = BattleUpdater.estimateSimulation(this, weAttack = true)
  lazy val estimationSimulationRetreat  : Estimation  = BattleUpdater.estimateSimulation(this, weAttack = false)
  
  //////////////
  // Features //
  //////////////
  
  def teams: Vector[Team] = Vector(us, enemy)
  
  def teamOf(unit: UnitInfo): Team = if (unit.isFriendly) us else enemy
  
  def focus: Pixel = teams.map(_.vanguard).centroid
  
  def happening: Boolean = teams.forall(_.units.nonEmpty) && teams.exists(_.units.exists(_.canAttack))
  
  ///////////////
  // Judgement //
  ///////////////
  
  lazy val desire: Double = {
    val nearestBaseOurs   = if (With.geography.ourBases.isEmpty)    With.geography.home.pixelCenter                     else With.geography.ourBases  .map(_.heart.pixelCenter).minBy(_.groundPixels(focus))
    val nearestBaseEnemy  = if (With.geography.enemyBases.isEmpty)  With.intelligence.mostBaselikeEnemyTile.pixelCenter else With.geography.enemyBases.map(_.heart.pixelCenter).minBy(_.groundPixels(focus))
    val urgencyOurs       = focus.pixelDistanceFast(nearestBaseEnemy)
    val urgencyEnemy      = focus.pixelDistanceFast(nearestBaseOurs)
    val fighters          = us.units.filter(_.canAttack)
    val aggressionDesire  = With.blackboard.aggressionRatio
    val flexibilityOurs   = meanFlexibility(us.units)
    val flexibilityEnemy  = meanFlexibility(enemy.units)
    val flexibilityRatio  = flexibilityOurs / flexibilityEnemy
    val urgencyRatio      = urgencyOurs / urgencyEnemy
    val chokiness         = if (us.centroid.zone == enemy.centroid.zone) 1.0 else 0.0
    val economyRatio      = With.geography.ourBases.size.toDouble / With.geography.enemyBases.size
    val flexibilityDesire = PurpleMath.clamp(flexibilityRatio,  0.9, 1.2)
    val urgencyDesire     = PurpleMath.clamp(urgencyRatio,      0.9, 1.5)
    val chokinessDesire   = PurpleMath.clamp(chokiness,         0.8, 1.0)
    val economyDesire     = PurpleMath.clamp(economyRatio,      0.9, 1.5)
    val bonusDesire       = aggressionDesire * flexibilityDesire * urgencyDesire * chokinessDesire * economyDesire
    val estimationAttack  = estimationSimulationAttack
    val estimationRetreat = estimationSimulationRetreat
    val attackGains       = estimationAttack.costToEnemy  + estimationRetreat.costToUs
    val attackLosses      = estimationAttack.costToUs     + estimationRetreat.costToEnemy
    val output            = bonusDesire * attackGains - attackLosses
    output
  }
  
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
  
  lazy val globalSafeToAttack: Boolean = {
    estimationAbstractOffense.weSurvive || estimationAbstractOffense.enemyDies || estimationAbstractOffense.netValue > 0
  }
}
