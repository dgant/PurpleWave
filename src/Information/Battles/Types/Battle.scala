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
  
  lazy val estimationAbstract         : Estimation  = BattleUpdater.estimateAvatar(this, geometric = false, weAttack = true,  enemyAttacks = true,  weRetreat = false)
  lazy val estimationAbstractOffense  : Estimation  = BattleUpdater.estimateAvatar(this, geometric = false, weAttack = true,  enemyAttacks = false, weRetreat = false)
  lazy val estimationAbstractDefense  : Estimation  = BattleUpdater.estimateAvatar(this, geometric = false, weAttack = false, enemyAttacks = true,  weRetreat = false)
  lazy val estimationSimulation       : Estimation  = BattleUpdater.estimateSimulation(this)
  
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
    val geographicDesire  = if (enemy.units.exists(_.unitClass.isSiegeTank)) Math.max(0.8, PurpleMath.nanToInfinity(urgencyOurs / urgencyEnemy)) else 1.0
    val estimation        = estimationSimulation
    val output            = With.blackboard.battleDesire * (estimation.costToEnemy * geographicDesire - estimation.costToUs) / estimation.frames / Math.max(1, fighters.size)
    output
  }
  
  lazy val globalSafeToAttack: Boolean = {
    estimationAbstractOffense.weSurvive || estimationAbstractOffense.enemyDies || estimationAbstractOffense.netValue > 0
  }
}
