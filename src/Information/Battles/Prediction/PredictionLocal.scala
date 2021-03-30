package Information.Battles.Prediction

import Information.Battles.Prediction.Simulation.{ReportCard, SimulationEvent}
import Information.Battles.Types.BattleLocal
import Lifecycle.With
import ProxyBwapi.UnitInfo.UnitInfo

import scala.collection.mutable

trait PredictionLocal {
  val battle: BattleLocal = this.asInstanceOf[BattleLocal]
  var simulationComplete: Boolean = false
  var simulationFrames = 0
  var simulationDeaths = 0.0
  var logSimulation: Boolean = With.configuration.debugging
  var simulationReport = new mutable.HashMap[UnitInfo, ReportCard]
  var simulationEvents: Iterable[SimulationEvent] = Iterable.empty
  val simulationCheckpoints: mutable.ArrayBuffer[SimulationCheckpoint] = new mutable.ArrayBuffer[SimulationCheckpoint]
}
