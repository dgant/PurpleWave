package Information.Battles.Prediction

import Information.Battles.Prediction.Simulation.{ReportCard, Simulation, SimulationEvent}
import Information.Battles.Types.BattleLocal
import ProxyBwapi.UnitInfo.UnitInfo

import scala.collection.mutable

class PredictionLocal(val battle: BattleLocal, val weAttack: Boolean, val weSnipe: Boolean) {

  val simulation: Simulation = new Simulation(this)

  var debugReport = new mutable.HashMap[UnitInfo, ReportCard]
  var events: Iterable[SimulationEvent] = Iterable.empty
  val localBattleMetrics: mutable.ArrayBuffer[LocalBattleMetrics] = new mutable.ArrayBuffer[LocalBattleMetrics]

  var frames          = 0
  var damageToUs      = 0.0
  var damageToEnemy   = 0.0
  var costToUs        = 0.0
  var costToEnemy     = 0.0
  var totalUnitsUs    = 0.0
  var totalUnitsEnemy = 0.0
  var deathsUs        = 0.0
  var deathsEnemy     = 0.0
}
