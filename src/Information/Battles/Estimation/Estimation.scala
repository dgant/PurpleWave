package Information.Battles.Estimation

class Estimation {

  var avatarUs      = new Avatar
  var avatarEnemy   = new Avatar
  var damageToUs    = 0.0
  var damageToEnemy = 0.0
  var costToUs      = 0.0
  var costToEnemy   = 0.0
  var deathsUs      = 0.0
  var deathsEnemy   = 0.0
  
  def netCost: Double = costToEnemy - costToUs
  
  def weGainValue : Boolean = costToEnemy  >   costToUs
  def weLoseValue : Boolean = costToEnemy  <   costToUs
  def weSurvive   : Boolean = deathsUs     <   avatarUs.totalUnits
  def weDie       : Boolean = deathsUs     >=  avatarEnemy.totalUnits
}
