package Information.Battles.Types

import Information.Battles.Prediction.LocalBattleMetrics
import Lifecycle.With
import Mathematics.PurpleMath
import Planning.UnitMatchers.UnitMatchSiegeTank
import ProxyBwapi.Races.{Protoss, Zerg}
import ProxyBwapi.UnitInfo.UnitInfo

class BattleJudgment(battle: BattleLocal) {

  val baseHysteresis  : Double  = if (With.self.isZerg) 1.5 else 1.0 // Maybe base this on simulated decisiveness
  val terranHomeBonus : Double  =   0.2 * getTerranHomeBonus
  val hysteresis      : Double  =   0.2 * getHysteresis * baseHysteresis
  val turtleBonus     : Double  =   0.1 * getTurtleBonus
  val hornetBonus     : Double  =   0.3 * getHornetBonus
  val terranMaxBonus  : Double  = - 0.2 * getTerranMaxBonus
  val siegeUrgency    : Double  = - 0.5 * getSiegeUrgency

  // "Ratios" here range on [-1, 1]
  val ratioAttack     : Double  = transformTotalScore(battle.predictionAttack.localBattleMetrics)
  val ratioSnipe      : Double  = if (battle.predictionSnipe == battle.predictionAttack) ratioAttack else transformTotalScore(battle.predictionSnipe.localBattleMetrics)
  val ratioBest       : Double  = Math.max(ratioAttack, ratioSnipe)
  val totalTarget     : Double  = hysteresis + terranHomeBonus + terranMaxBonus + turtleBonus + hornetBonus + siegeUrgency + With.configuration.baseThreshold
  val ratioThreshold  : Double  = Math.min(1, PurpleMath.nanToZero(totalTarget))
  val confidence      : Double  = PurpleMath.nanToN((ratioBest - ratioThreshold) / Math.abs(Math.signum(ratioBest - ratioThreshold) - ratioThreshold), if (ratioBest >= ratioThreshold) 1 else -1)
  val shouldAttack    : Boolean = ratioAttack >= ratioThreshold
  val shouldSnipe     : Boolean = ratioSnipe >= ratioThreshold
  val shouldFight     : Boolean = shouldAttack || shouldSnipe

  def getTerranHomeBonus: Double = {
    if (battle.enemy.centroidAir.zone.owner.isTerran && battle.enemy.units.exists(u => u.is(UnitMatchSiegeTank) && u.matchups.targets.nonEmpty)) 0.2 else 0.0
  }
  def getTerranMaxBonus: Double = {
    if (With.enemies.forall(_.isTerran)) PurpleMath.clamp(0.0, 1.0, (With.self.supplyUsed - 300) / 100.0) else 0
  }
  def getTurtleBonus: Double = {
    // Should we turtle behind our defenses?
    val turrets = battle.us.units.view.filter(u => ! u.canMove && u.canAttack)
    if (turrets.isEmpty) return 0.0
    if (turrets.forall(t => t.matchups.targetsInRange.isEmpty && t.matchups.threatsInRange.isEmpty)) return 1.0
    0.0
  }
  def getHornetBonus: Double = {
    // Avoid poking at hornet nests
    val hornets = battle.enemy.units.count(u =>
      u.isSiegeTankSieged()
      && u.matchups.targets.nonEmpty
      && (u.base.exists(_.owner.isEnemy) || u.zone.exit.exists(_.otherSideof(u.zone).bases.exists(_.owner.isEnemy)))
      && (u.matchups.threatsInRange.isEmpty || ! u.visible))
    val home = battle.us.units.count(_.base.exists(_.owner.isUs))
    hornets.toDouble / Math.max(1, hornets + home)
  }
  def getSiegeUrgency: Double = {
    val eligibleUnits = battle.enemy.units.view.filter(_.isAny(UnitMatchSiegeTank, Protoss.Reaver, Zerg.Lurker))
    if (eligibleUnits.isEmpty) return 0.0
    val distanceUs     : Double  = eligibleUnits.map(_.pixelDistanceTravelling(With.geography.home)).min
    val distanceEnemy  : Double  = eligibleUnits.map(_.pixelDistanceTravelling(With.scouting.mostBaselikeEnemyTile)).max
    val distanceRatio  : Double  = distanceEnemy / (distanceUs + distanceEnemy)
    distanceRatio
  }

  def transformTotalScore(metrics: Seq[LocalBattleMetrics]): Double = {
    // This can happen when all simulated enemies run away and nobody does any damage
    if (metrics.lastOption.forall(metric => metric.localHealthLostUs <= 0)) return 1.0

    val average = PurpleMath.weightedMean(metrics.view.map(m => (m.totalScore, m.cumulativeTotalDecisiveness)))
    val aggression = With.blackboard.aggressionRatio()
    val output = PurpleMath.clamp(
      aggression * (1.0 + average) - 1.0,
      -1.0,
      1.0)
    output
  }

  def getHysteresis: Double = {
    PurpleMath.weightedMean(battle.us.units.filter(_.canMove).map(u => (unitHysteresis(u), u.subjectiveValue)))
  }

  def unitHysteresis(unit: UnitInfo): Double = {
    if (unit.friendly.isEmpty) return 0.0
    val agent               = unit.friendly.get.agent
    val patienceHysteresis  = agent.combatHysteresisFrames.toDouble / With.configuration.battleHysteresisFrames
    val patienceEntangled   = if (agent.shouldEngage) Math.max(0.0, PurpleMath.nanToZero(unit.matchups.pixelsOfEntanglement / unit.topSpeed)) / With.configuration.battleHysteresisFrames else 0.0
    val patienceTotal       = Math.max(patienceEntangled, patienceHysteresis)
    val sign                = if (agent.shouldEngage) -1.0 else 1.0
    var output              = patienceTotal * sign * (if(unit.unitClass.melee) 2.0 else 1.0)
    if (output < 0 && unit.matchups.framesOfSafety > 0) {
      output = 0
    }
    output
  }
}
