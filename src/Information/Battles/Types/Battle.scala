package Information.Battles.Types

import Information.Battles.Prediction.Simulation.{ReportCard, SimulationEvent}
import Information.Battles.Prediction.SimulationCheckpoint
import Lifecycle.With
import Mathematics.Maff
import Mathematics.Points.Pixel
import ProxyBwapi.UnitInfo.UnitInfo

import scala.collection.mutable

class Battle(unitsUs: Seq[UnitInfo] = Vector.empty, unitsEnemy: Seq[UnitInfo] = Vector.empty, val isGlobal: Boolean) {
  val frameCreated: Int = With.frame
  val us = new Team(this, unitsUs)
  val enemy = new Team(this, unitsEnemy)
  val teams: Vector[Team] = Vector(us, enemy)
  val focus: Pixel = Maff.centroid(teams.map(_.vanguardAll()))

  var predictionComplete: Boolean = false

  def units: Seq[UnitInfo] = us.units.view ++ enemy.units

  //////////////////////////
  // Prediction arguments //
  //////////////////////////

  val skimulated: Boolean = isGlobal || With.configuration.skimulate
  val logSimulation: Boolean = With.configuration.debugging
  val judgmentModifiers: Seq[JudgmentModifier] = JudgmentModifiers(this)
  val speedMultiplier: Double = if (isGlobal) 1.0 else judgmentModifiers.map(_.speedMultiplier).product

  ////////////////////////
  // Simulation results //
  ////////////////////////

  var judgement: Option[BattleJudgment] = None
  var simulationFrames = 0
  var simulationDeaths = 0.0
  var simulationReport = new mutable.HashMap[UnitInfo, ReportCard]
  var simulationEvents: Iterable[SimulationEvent] = Iterable.empty
  val simulationCheckpoints: mutable.ArrayBuffer[SimulationCheckpoint] = new mutable.ArrayBuffer[SimulationCheckpoint]
}
