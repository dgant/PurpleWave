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
  
  lazy val estimationAbstract         : Estimation  = BattleUpdater.estimate(this, geometric = false, weAttack = true,  enemyAttacks = true,  weRetreat = false)
  lazy val estimationAbstractOffense  : Estimation  = BattleUpdater.estimate(this, geometric = false, weAttack = true,  enemyAttacks = false, weRetreat = false)
  lazy val estimationAbstractDefense  : Estimation  = BattleUpdater.estimate(this, geometric = false, weAttack = false, enemyAttacks = true,  weRetreat = false)
  lazy val estimationGeometric        : Estimation  = BattleUpdater.estimate(this, geometric = false, weAttack = true,  enemyAttacks = true,  weRetreat = false)
  lazy val estimationGeometricOffense : Estimation  = BattleUpdater.estimate(this, geometric = true,  weAttack = true,  enemyAttacks = false, weRetreat = false)
  lazy val estimationGeometricDefense : Estimation  = BattleUpdater.estimate(this, geometric = true,  weAttack = false, enemyAttacks = true,  weRetreat = false)
  lazy val estimationGeometricRetreat : Estimation  = BattleUpdater.estimate(this, geometric = true,  weAttack = false, enemyAttacks = false, weRetreat = true)
  
  //////////////
  // Features //
  //////////////
  
  def teams: Vector[Team] = Vector(us, enemy)
  
  def focus: Pixel = teams.map(_.vanguard).centroid
  
  def happening: Boolean = teams.forall(_.units.nonEmpty) && teams.exists(_.units.exists(_.canAttackThisSecond))
  
  ///////////////
  // Judgement //
  ///////////////
  
  lazy val shouldAttack: Boolean = {
    estimationGeometricOffense.weSurvive
    //estimationGeometricOffense.netValue > estimationGeometricRetreat.netValue
  }
  
  lazy val shouldRetreat: Boolean = {
    estimationGeometricOffense.weDie
    //estimationGeometricOffense.netValue < estimationGeometricRetreat.netValue
  }
}
