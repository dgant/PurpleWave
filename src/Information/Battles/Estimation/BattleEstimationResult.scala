package Information.Battles.Estimation

class BattleEstimationResult {
  var frames      = 0.0
  var costToUs    = 0.0
  var costToEnemy = 0.0
  
  def netCost:Double = costToEnemy - costToUs
}
