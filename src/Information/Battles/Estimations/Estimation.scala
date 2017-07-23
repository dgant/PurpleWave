package Information.Battles.Estimations

import Lifecycle.With

class Estimation {

  val frameCalculated: Int = With.frame
  
  var avatarUs      = new Avatar
  var avatarEnemy   = new Avatar
  var damageToUs    = 0.0
  var damageToEnemy = 0.0
  var costToUs      = 0.0
  var costToEnemy   = 0.0
  var deathsUs      = 0.0
  var deathsEnemy   = 0.0
  
  def netValue: Double = costToEnemy - costToUs
  
  def weGainValue     : Boolean = costToEnemy  >   costToUs
  def weLoseValue     : Boolean = costToEnemy  <   costToUs
  def weSurvive       : Boolean = deathsUs     <   avatarUs.totalUnits    || deathsUs == 0
  def weDie           : Boolean = deathsUs     >=  avatarEnemy.totalUnits && deathsUs > 0
  def enemyGainValue  : Boolean = costToUs     >   costToEnemy
  def enemyLoseValue  : Boolean = costToUs     <   costToEnemy
  def enemySurvives   : Boolean = deathsEnemy  <   avatarEnemy.totalUnits || deathsEnemy == 0
  def enemyDies       : Boolean = deathsEnemy  >=  avatarUs.totalUnits    && deathsEnemy > 0
}
