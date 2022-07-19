package Information.Battles.Types

import Information.Battles.Prediction.Simulation.{ReportCard, SimulationEvent}
import Information.Battles.Prediction.SimulationCheckpoint
import Lifecycle.With
import Mathematics.Maff
import Mathematics.Points.Pixel
import ProxyBwapi.UnitInfo.UnitInfo

import scala.collection.mutable

class Battle(unitsUs: Seq[UnitInfo] = Vector.empty, unitsEnemy: Seq[UnitInfo] = Vector.empty, val isGlobal: Boolean) {
  lazy val frameCreated: Int = With.frame
  lazy val us     = new Team(this, unitsUs)
  lazy val enemy  = new Team(this, unitsEnemy)
  lazy val teams: Vector[Team] = Vector(us, enemy)
  lazy val focus: Pixel = Maff.centroid(teams.map(_.vanguardAll()))

  var predictionComplete: Boolean = false

  def units: Seq[UnitInfo] = us.units.view ++ enemy.units

  //////////////////////////
  // Prediction arguments //
  //////////////////////////

  lazy val skimulated       : Boolean               = isGlobal || With.configuration.skimulate
  lazy val logSimulation    : Boolean               = With.configuration.debugging
  lazy val speedMultiplier  : Double                = if (isGlobal) 1.0 else judgmentModifiers.map(_.speedMultiplier).product
  lazy val judgmentModifiers: Seq[JudgmentModifier] = if (isGlobal) Seq.empty else JudgmentModifiers(this)


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
