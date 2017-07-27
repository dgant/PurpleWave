package Information.Battles.Estimations

import Information.Battles.Estimations.Simulation.Simulation
import Lifecycle.With
import ProxyBwapi.UnitInfo.UnitInfo

import scala.collection.mutable

class Estimation {

  val frameCalculated: Int = With.frame
  
  var frames            = 0
  var avatarUs          = new Avatar
  var avatarEnemy       = new Avatar
  var damageToUs        = 0.0
  var damageToEnemy     = 0.0
  var costToUs          = 0.0
  var costToEnemy       = 0.0
  var totalUnitsUs      = 0.0
  var totalUnitsEnemy   = 0.0
  var deathsUs          = 0.0
  var deathsEnemy       = 0.0
  
  var simulation: Option[Simulation] = None
  
  var reportCards = new mutable.HashMap[UnitInfo, ReportCard]
  
  def netValue: Double = costToEnemy - costToUs
  
  def weGainValue     : Boolean = costToEnemy  >   costToUs
  def weLoseValue     : Boolean = costToEnemy  <   costToUs
  def weSurvive       : Boolean = deathsUs     <   totalUnitsUs || deathsUs == 0
  def weDie           : Boolean = ! weSurvive
  def enemyGainValue  : Boolean = costToUs     >   costToEnemy
  def enemyLoseValue  : Boolean = costToUs     <   costToEnemy
  def enemySurvives   : Boolean = deathsEnemy  <   totalUnitsEnemy || deathsEnemy == 0
  def enemyDies       : Boolean = ! enemySurvives
}
