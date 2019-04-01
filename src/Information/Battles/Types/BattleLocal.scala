package Information.Battles.Types

import Information.Battles.Prediction.{LocalBattleMetrics, Prediction}
import Information.Battles.Prediction.Simulation.Simulation
import Lifecycle.With
import Mathematics.PurpleMath
import Planning.UnitMatchers.UnitMatchSiegeTank
import ProxyBwapi.Races.Zerg
import ProxyBwapi.UnitInfo.UnitInfo
import Utilities.ByOption

class BattleLocal(us: Team, enemy: Team) extends Battle(us, enemy) {
  
  lazy val estimationSimulationAttack: Prediction = estimateSimulation(this, weAttack = true, weSnipe = false)
  lazy val estimationSimulationSnipe: Prediction =
    if (With.self.isZerg)
      estimateSimulation(this, weAttack = true, weSnipe = true)
    else
      estimationSimulationAttack
  
  lazy val turrets                  = us.units.filter(u => ! u.canMove && u.canAttack)
  lazy val canTurtle      : Boolean = turrets.nonEmpty && turrets.forall(t => t.matchups.targetsInRange.isEmpty && t.matchups.threatsInRange.isEmpty)
  lazy val terranBonus    : Double  = if (enemy.centroid.zone.owner.isTerran && enemy.units.exists(_.unitClass.isSiegeTank)) 0.2 else 0.0
  lazy val hysteresis     : Double  = PurpleMath.weightedMean(us.units.filter(_.canMove).map(u => (hysteresisRatio(u), u.subjectiveValue)))
  lazy val rangeEnemy     : Double  = ByOption.max(enemy.units.map(_.pixelRangeMax)).getOrElse(0.0) // Sally out to meet siege units
  lazy val distanceUs     : Double  = focus.pixelDistance(With.geography.home.pixelCenter)
  lazy val distanceEnemy  : Double  = focus.pixelDistance(With.intelligence.mostBaselikeEnemyTile.pixelCenter) + 2 * rangeEnemy
  lazy val distanceRatio  : Double  = distanceEnemy / (distanceUs + distanceEnemy)
  lazy val turtleBonus    : Double  = if (canTurtle) 0.1 else 0.0
  lazy val urgency   : Double  = if (enemy.units.exists(_.isAny(UnitMatchSiegeTank, Zerg.Lurker))) - PurpleMath.clamp(distanceRatio - 0.6, 0.0, 0.4) else 0.0
  lazy val trappedness    : Double  = PurpleMath.weightedMean(us.units.filter(u => u.canMove || u.sieged).map(u => (trappedPenalty(u), u.subjectiveValue)))

  def transformTotalScore(metrics: Seq[LocalBattleMetrics]): Double = {
    val average = PurpleMath.weightedMean(
      metrics.map(metric => (
        metric.totalScore,
        // Weigh by decisiveness and ange
        Math.max(1e-5, metric.totalDecisiveness * Math.pow(0.5, metric.framesIn / With.configuration.simulationScoreHalfLife)))))
    val aggression = With.blackboard.aggressionRatio()
    val output = PurpleMath.clamp(
      average + (1.0 - average) * (aggression - 1.0),
      -1.0,
      1.0)
    output
  }

  lazy val ratioAttack    : Double  = transformTotalScore(estimationSimulationAttack.localBattleMetrics)
  lazy val ratioSnipe     : Double  = transformTotalScore(estimationSimulationSnipe.localBattleMetrics)
  lazy val totalTarget    : Double  = hysteresis + terranBonus + turtleBonus + urgency + trappedness + With.configuration.baseTarget
  lazy val ratioTarget    : Double  = Math.min(.99, PurpleMath.nanToZero(totalTarget))
  lazy val shouldFight    : Boolean = ratioAttack > ratioTarget || (With.self.isZerg && ratioSnipe > ratioTarget)
  
  private def estimateSimulation(
    battle: BattleLocal,
    weAttack: Boolean,
    weSnipe: Boolean,
    onlyFlyersAttack: Boolean = false,
    onlyWalkersAttack: Boolean = false): Prediction = {
    val simulation = new Simulation(battle, weAttack, weSnipe)
    simulation.run()
    simulation.estimation
  }

  private def trappedPenalty(unit: UnitInfo): Double = {
    if (unit.flying) return 0.0
    if (unit.friendly.isEmpty) return 0.0
    if (unit.sieged) return 1.0
    val agent = unit.friendly.get.agent
    val trapped = (
      ! unit.canMove
      || unit.matchups.doomed
      || unit.base.exists(base => With.geography.ourBases.contains(base) || unit.friendly.exists(_.agent.toForm.exists(_.base.contains(base))))
      || unit.matchups.threats.exists(t => t.topSpeed > unit.topSpeed && ! t.unitClass.isWorker && t.inRangeToAttack(unit)))
    if ( ! trapped) return 0.0
    val safetyFrames        = 72
    val factorSafety        = PurpleMath.clamp((safetyFrames - unit.matchups.framesOfSafety) / safetyFrames, 0.0, 2.0)
    val factorEntanglement  = PurpleMath.clamp(PurpleMath.nanToZero(unit.matchups.framesOfEntanglement / unit.matchups.framesToLive), 0.0, 2.0)
    val factor              = (factorSafety + factorEntanglement) / 2.0
    val output              = -0.25 * PurpleMath.clamp(factor, 0.0, 1.0)
    output
  }
  
  private def hysteresisRatio(unit: UnitInfo): Double = {
    if (unit.friendly.isEmpty) return 0.0
    val agent               = unit.friendly.get.agent
    val patienceHysteresis  = agent.combatHysteresisFrames.toDouble / With.configuration.battleHysteresisFrames
    val patienceEntangled   = if (agent.shouldEngage) Math.max(0.0, unit.matchups.framesOfEntanglement) / With.configuration.battleHysteresisFrames else 0.0
    val patienceTotal       = Math.max(patienceEntangled, patienceHysteresis)
    val sign                = if (agent.shouldEngage) -1.0 else 1.0
    var output              = patienceTotal * sign * With.configuration.battleHysteresisRatio * (if(unit.unitClass.melee) 2.0 else 1.0)
    if (output < 0 && unit.matchups.framesOfSafety > 0) {
      output = 0
    }
    output
  }
}
