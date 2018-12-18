package Information.Battles.Types

import Information.Battles.Prediction.Prediction
import Information.Battles.Prediction.Simulation.Simulation
import Lifecycle.With
import Mathematics.PurpleMath
import ProxyBwapi.UnitInfo.UnitInfo
import Utilities.ByOption

class BattleLocal(us: Team, enemy: Team) extends Battle(us, enemy) {
  
  lazy val estimationSimulationAttack: Prediction = estimateSimulation(this, weAttack = true, weSnipe = false)
  lazy val estimationSimulationSnipe: Prediction =
    if (With.self.isZerg)
      estimateSimulation(this, weAttack = true, weSnipe = true)
    else
      estimationSimulationAttack
  
  lazy val turrets                  = us.units.filter(u => ! u.canMove && ! u.canAttack)
  lazy val canTurtle      : Boolean = turrets.nonEmpty && turrets.forall(t => t.matchups.targetsInRange.isEmpty && t.matchups.threatsInRange.isEmpty)
  lazy val hysteresis     : Double  = PurpleMath.weightedMean(us.units.filter(_.canMove).map(u => (hysteresisRatio(u), u.subjectiveValue)))
  lazy val rangeEnemy     : Double  = ByOption.max(enemy.units.map(_.pixelRangeMax)).getOrElse(0.0) // Sally out to meet siege units
  lazy val distanceUs     : Double  = focus.pixelDistance(With.geography.home.pixelCenter)
  lazy val distanceEnemy  : Double  = focus.pixelDistance(With.intelligence.mostBaselikeEnemyTile.pixelCenter) + 2 * rangeEnemy
  lazy val distanceRatio  : Double  = distanceEnemy / (distanceUs + distanceEnemy)
  lazy val urgency        : Double  = if (canTurtle) 0.0 else PurpleMath.clamp(0.75 * (distanceRatio - 0.5), 0.0, 0.5)
  lazy val attackGains    : Double  = estimationSimulationAttack.costToEnemy
  lazy val attackLosses   : Double  = estimationSimulationAttack.costToUs
  lazy val snipeGains     : Double  = estimationSimulationSnipe.costToEnemy
  lazy val snipeLosses    : Double  = estimationSimulationSnipe.costToUs
  lazy val ratioAttack    : Double  = PurpleMath.nanToInfinity(attackGains / (attackGains + attackLosses))
  lazy val ratioSnipe     : Double  = PurpleMath.nanToInfinity(snipeGains / (snipeGains + snipeLosses))
  lazy val ratioTarget    : Double  = Math.min(2.0, PurpleMath.nanToZero((With.battles.global.valueRatioTarget + hysteresis - urgency) / With.blackboard.aggressionRatio()))
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
  
  private def hysteresisRatio(unit: UnitInfo): Double = {
    if (unit.friendly.isEmpty) return 0.0
    val agent               = unit.friendly.get.agent
    val patienceHysteresis  = agent.combatHysteresisFrames.toDouble / With.configuration.battleHysteresisFrames
    val patienceEntangled   = if (agent.shouldEngage) Math.max(0.0, unit.matchups.framesOfEntanglement) / With.configuration.battleHysteresisFrames else 0.0
    val patienceTotal       = Math.max(patienceEntangled, patienceHysteresis)
    val sign                = if (agent.shouldEngage) -1.0 else 1.0
    val output              = patienceTotal * sign * With.configuration.battleHysteresisRatio * (if(unit.unitClass.melee) 2.0 else 1.0)
    output
  }
}
