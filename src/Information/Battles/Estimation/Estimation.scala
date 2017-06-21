package Information.Battles.Estimation

class Estimation {

  var damageToUs    = 0.0
  var damageToEnemy = 0.0
  var costToUs      = 0.0
  var costToEnemy   = 0.0
  var deathsUs      = 0.0
  var deathsEnemy   = 0.0
  
  def netCost: Double = costToEnemy - costToUs
}
