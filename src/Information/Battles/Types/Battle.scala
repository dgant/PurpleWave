package Information.Battles.Types

import Information.Battles.BattleUpdater
import Information.Battles.Estimations.Estimation
import Lifecycle.With
import Mathematics.Points.Pixel
import ProxyBwapi.UnitInfo.UnitInfo
import Utilities.EnrichPixel.EnrichedPixelCollection

class Battle(
  val us    : Team,
  val enemy : Team) {
  
  us.battle     = this
  enemy.battle  = this
  
  //////////////
  // Features //
  //////////////
  
  def teams: Vector[Team] = Vector(us, enemy)
  def teamOf(unit: UnitInfo): Team = if (unit.isFriendly) us else enemy
  def focus: Pixel = teams.map(_.vanguard).centroid
  def happening: Boolean = teams.forall(_.units.nonEmpty) && teams.exists(_.units.exists(_.canAttack))
  
  /////////////////
  // Estimations //
  /////////////////
  
  lazy val estimationAbstract           : Estimation  = BattleUpdater.estimateAvatar(this, geometric = false, weAttack = true,  enemyAttacks = true,  weRetreat = false)
  lazy val estimationAbstractOffense    : Estimation  = BattleUpdater.estimateAvatar(this, geometric = false, weAttack = true,  enemyAttacks = false, weRetreat = false)
  lazy val estimationAbstractDefense    : Estimation  = BattleUpdater.estimateAvatar(this, geometric = false, weAttack = false, enemyAttacks = true,  weRetreat = false)
  lazy val estimationSimulationAttack   : Estimation  = BattleUpdater.estimateSimulation(this, weAttack = true)
  lazy val estimationSimulationRetreat  : Estimation  = BattleUpdater.estimateSimulation(this, weAttack = false)
  
  ///////////////
  // Judgement //
  ///////////////
  
  lazy val analysis = new BattleAnalysis(this)
  lazy val desire: Double = analysis.desireTotal
  
  lazy val globalSafeToAttack: Boolean = globalSafe(estimationAbstractOffense, With.blackboard.aggressionRatio)
  lazy val globalSafeToDefend: Boolean = globalSafe(estimationAbstractDefense, With.blackboard.safetyRatio) || globalSafeToAttack
  
  private def globalSafe(estimation: Estimation, discountFactor: Double): Boolean = {
    val tradesEffectively = discountFactor * estimation.costToEnemy - estimation.costToUs >= 0
    val killsEffectively  =
      estimation.enemyDies &&
      estimation.weSurvive &&
      estimation.costToUs < estimationAbstractOffense.avatarUs.subjectiveValue
    
    tradesEffectively || killsEffectively
  }
  
  
}
