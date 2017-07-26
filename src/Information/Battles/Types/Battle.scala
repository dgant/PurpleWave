package Information.Battles.Types

import Information.Battles.BattleUpdater
import Information.Battles.Estimations.Estimation
import Lifecycle.With
import Mathematics.Points.Pixel
import Mathematics.PurpleMath
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
  
  lazy val desire: Double = {
    val nearestBaseOurs   = if (With.geography.ourBases.isEmpty)    With.geography.home.pixelCenter                     else With.geography.ourBases  .map(_.heart.pixelCenter).minBy(_.groundPixels(focus))
    val nearestBaseEnemy  = if (With.geography.enemyBases.isEmpty)  With.intelligence.mostBaselikeEnemyTile.pixelCenter else With.geography.enemyBases.map(_.heart.pixelCenter).minBy(_.groundPixels(focus))
    val urgencyOurs       = focus.pixelDistanceFast(nearestBaseEnemy)
    val urgencyEnemy      = focus.pixelDistanceFast(nearestBaseOurs)
    val fighters          = us.units.filter(_.canAttackThisSecond)
    val directDesire      = estimationGeometricOffense.costToEnemy / Math.max(1.0, estimationGeometricOffense.costToUs)
    val geographicDesire  = PurpleMath.nanToInfinity(urgencyOurs / urgencyEnemy)
    val output            = directDesire * geographicDesire
    output
  }
  
  lazy val globalSafeToAttack: Boolean = {
    estimationAbstractOffense.weSurvive || estimationAbstractOffense.enemyDies || estimationAbstractOffense.netValue > 0
  }
}
