package Information.Battles.Types

import Information.Battles.Prediction.LocalBattleMetrics
import Lifecycle.With
import Mathematics.PurpleMath
import Planning.UnitMatchers.UnitMatchSiegeTank
import ProxyBwapi.Races.{Protoss, Zerg}
import ProxyBwapi.UnitInfo.UnitInfo

class BattleJudgment(battle: BattleLocal) {

  lazy val baseHysteresis   : Double  = if (With.self.isZerg) 1.5 else 1.0
  lazy val terranHomeBonus  : Double  =   0.2 * getTerranHomeBonus
  lazy val hysteresis       : Double  =   0.2 * getHysteresis * baseHysteresis
  lazy val turtleBonus      : Double  =   0.1 * getTurtleBonus
  lazy val hornetBonus      : Double  =   0.3 * getHornetBonus
  lazy val terranMaxBonus   : Double  = - 0.2 * getTerranMaxBonus
  lazy val siegeUrgency     : Double  = - 0.5 * getSiegeUrgency
  lazy val trappedness      : Double  = - 0.2 * getTrappedness
  lazy val meleeCommitment  : Double  =   0.2 * getSkirmishModifier
  lazy val ratioAttack      : Double  = transformTotalScore(battle.estimationSimulationAttack.get.localBattleMetrics)
  lazy val ratioSnipe       : Double  = transformTotalScore(battle.estimationSimulationSnipe.get.localBattleMetrics)
  lazy val totalTarget      : Double  = hysteresis + terranHomeBonus + terranMaxBonus + turtleBonus + hornetBonus + siegeUrgency + trappedness + With.configuration.baseTarget
  lazy val ratioTarget      : Double  = Math.min(.99, PurpleMath.nanToZero(totalTarget))
  lazy val shouldFight      : Boolean = ratioAttack > ratioTarget || ratioSnipe > ratioTarget

  def getTerranHomeBonus: Double = {
    if (battle.enemy.centroid.zone.owner.isTerran && battle.enemy.units.exists(u => u.is(UnitMatchSiegeTank) && u.matchups.targets.nonEmpty)) 0.2 else 0.0
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
  def getTrappedness: Double = {
    PurpleMath.nanToZero(
      PurpleMath.weightedMean(battle.us.units.view.map(unit =>
        ({
          val entanglement = PurpleMath.clamp(unit.matchups.framesOfEntanglement, -72, 72)
          val entanglementMin = -24
          val entanglementMax = 24 + PurpleMath.clamp(unit.matchups.framesToLive, 0, 72)
          Math.max(0, (entanglement - entanglementMin) / (entanglementMax - entanglementMin))
        },
        unit.subjectiveValue))))
  }
  def getSiegeUrgency: Double = {
    val eligibleUnits = battle.enemy.units.view.filter(_.isAny(UnitMatchSiegeTank, Protoss.Reaver, Zerg.Lurker))
    if (eligibleUnits.isEmpty) return 0.0
    val distanceUs     : Double  = eligibleUnits.map(_.pixelDistanceTravelling(With.geography.home)).min
    val distanceEnemy  : Double  = eligibleUnits.map(_.pixelDistanceTravelling(With.scouting.mostBaselikeEnemyTile)).max
    val distanceRatio  : Double  = distanceEnemy / (distanceUs + distanceEnemy)
    distanceRatio
  }
  private def meanRange(team: Team): Double = PurpleMath.weightedMean(team.units.view.map(u => (u.effectiveRangePixels, u.subjectiveValue)))
  private def outranges(unit: UnitInfo, threat: UnitInfo): Boolean = threat.pixelRangeAgainst(unit) > unit.effectiveRangePixels
  def getSkirmishModifier: Double = {
    val meanRangeUs     = meanRange(battle.us)

    val meanRangeEnemy  = meanRange(battle.enemy)
    val outrangeableUs  = battle.us.units.count(unit => unit.matchups.threats.exists(outranges(unit, _)))
    val outrangedUs     = battle.us.units.count(unit => unit.matchups.threatsInRange.exists(outranges(unit, _)))
    val ratioRange      = PurpleMath.nanToZero(meanRangeEnemy - meanRangeUs / (meanRangeEnemy + meanRangeUs))
    val ratioOutranged  = PurpleMath.nanToZero(2 * outrangedUs / outrangeableUs - 1)
    //val impact          = PurpleMath.nanToZero(outrangeableUs / team
    0.0
  }

  def transformTotalScore(metrics: Seq[LocalBattleMetrics]): Double = {
    // This can happen when all simulated enemies run away and nobody does any damage
    if (metrics.lastOption.exists(metric => metric.localHealthLostUs <= 0)) return 1.0

    val average = PurpleMath.weightedMean(
      metrics.map(metric => (
        metric.totalScore,
        // Weigh by decisiveness and ange
        Math.max(1e-5, metric.totalDecisiveness * Math.pow(0.5, metric.framesIn / With.configuration.simulationScoreHalfLife)))))
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
    val patienceEntangled   = if (agent.shouldEngage) Math.max(0.0, unit.matchups.framesOfEntanglement) / With.configuration.battleHysteresisFrames else 0.0
    val patienceTotal       = Math.max(patienceEntangled, patienceHysteresis)
    val sign                = if (agent.shouldEngage) -1.0 else 1.0
    var output              = patienceTotal * sign * (if(unit.unitClass.melee) 2.0 else 1.0)
    if (output < 0 && unit.matchups.framesOfSafety > 0) {
      output = 0
    }
    output
  }
}
