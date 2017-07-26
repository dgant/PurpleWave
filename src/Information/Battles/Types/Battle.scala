package Information.Battles.Types

import Information.Battles.BattleUpdater
import Information.Battles.Estimations.Estimation
import Mathematics.Points.Pixel
import Utilities.EnrichPixel.EnrichedPixelCollection

class Battle(
  val us    : Team,
  val enemy : Team) {
  
  us.battle     = this
  enemy.battle  = this
  
  /////////////////
  // Estimations //
  /////////////////
  
  lazy val estimationAbstract         : Estimation  = BattleUpdater.estimateAvatar(this, geometric = false, weAttack = true,  enemyAttacks = true,  weRetreat = false)
  lazy val estimationAbstractOffense  : Estimation  = BattleUpdater.estimateAvatar(this, geometric = false, weAttack = true,  enemyAttacks = false, weRetreat = false)
  lazy val estimationAbstractDefense  : Estimation  = BattleUpdater.estimateAvatar(this, geometric = false, weAttack = false, enemyAttacks = true,  weRetreat = false)
  lazy val estimationGeometric        : Estimation  = BattleUpdater.estimateAvatar(this, geometric = false, weAttack = true,  enemyAttacks = true,  weRetreat = false)
  lazy val estimationGeometricOffense : Estimation  = BattleUpdater.estimateAvatar(this, geometric = true,  weAttack = true,  enemyAttacks = false, weRetreat = false)
  lazy val estimationGeometricDefense : Estimation  = BattleUpdater.estimateAvatar(this, geometric = true,  weAttack = false, enemyAttacks = true,  weRetreat = false)
  lazy val estimationGeometricRetreat : Estimation  = BattleUpdater.estimateAvatar(this, geometric = true,  weAttack = false, enemyAttacks = true,  weRetreat = true)
  lazy val estimationMatchups         : Estimation  = BattleUpdater.estimateMatchups(this)
  
  //////////////
  // Features //
  //////////////
  
  def teams: Vector[Team] = Vector(us, enemy)
  
  def focus: Pixel = teams.map(_.vanguard).centroid
  
  def happening: Boolean = teams.forall(_.units.nonEmpty) && teams.exists(_.units.exists(_.canAttackThisSecond))
  
  ///////////////
  // Judgement //
  ///////////////
  
  lazy val localAttackDesire: Double = {
    estimationGeometricOffense.costToEnemy / Math.max(1.0, estimationGeometricOffense.costToUs)
  }
  
  lazy val globalSafeToAttack: Boolean = {
    estimationAbstractOffense.weSurvive || estimationAbstractOffense.enemyDies || estimationAbstractOffense.netValue > 0
  }
}
