package Information.Battles.Prediction

import Information.Battles.Prediction.Estimation.Avatar
import Information.Battles.Prediction.Simulation.{ReportCard, Simulation, SimulationEvent}
import Lifecycle.With
import ProxyBwapi.UnitInfo.UnitInfo

import scala.collection.mutable

class Prediction {

  val frameCalculated: Int = With.frame

  // Used only for global battles
  var avatarUs    = new Avatar
  var avatarEnemy = new Avatar

  // Used only for local battles
  var simulation: Option[Simulation] = None
  var reportCards = new mutable.HashMap[UnitInfo, ReportCard]
  var events: Iterable[SimulationEvent] = Iterable.empty
  val localBattleMetrics: mutable.ArrayBuffer[LocalBattleMetrics] = new mutable.ArrayBuffer[LocalBattleMetrics]

  // Old (Pre-2019) metrics
  def weSurvive       : Boolean = deathsUs    < totalUnitsUs || deathsUs == 0
  def enemySurvives   : Boolean = deathsEnemy < totalUnitsEnemy || deathsEnemy == 0
  def enemyDies       : Boolean = ! enemySurvives
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
