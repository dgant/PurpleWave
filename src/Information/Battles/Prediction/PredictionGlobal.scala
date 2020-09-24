package Information.Battles.Prediction

import Information.Battles.Prediction.Estimation.Avatar

class PredictionGlobal(val avatarUs: Avatar, val avatarEnemy: Avatar) {
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
