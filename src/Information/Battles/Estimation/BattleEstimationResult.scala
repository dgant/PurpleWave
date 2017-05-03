package Information.Battles.Estimation

class BattleEstimationResult {
  var costToUs  = 0.0
  var costToEnemy = 0.0
  
  def netCost:Double = costToEnemy - costToUs
}
