package Information.Battles.Estimation

import scala.collection.mutable.ArrayBuffer

class BattleEstimationResult {

  var frames      = 0.0
  var costToUs    = 0.0
  var costToEnemy = 0.0
  
  def netCost:Double = costToEnemy - costToUs
  
  val statesUs    = new ArrayBuffer[BattleEstimationCalculationState]
  val statesEnemy = new ArrayBuffer[BattleEstimationCalculationState]
}
