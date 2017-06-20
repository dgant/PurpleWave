package ProxyBwapi.UnitInfo

import Lifecycle.With
import Micro.Execution.ExecutionState
import ProxyBwapi.Techs.{Tech, Techs}
import ProxyBwapi.Upgrades.{Upgrade, Upgrades}

class FriendlyUnitInfo(base:bwapi.Unit) extends FriendlyUnitProxy(base) {
  
  override def friendly: Option[FriendlyUnitInfo] = Some(this)
  
  ////////////
  // Health //
  ////////////
  
  //////////////
  // Geometry //
  //////////////
  
  ////////////
  // Orders //
  ////////////
  
  def getBuildUnit  : Option[UnitInfo]    = With.units.get(base.getBuildUnit)
  def techingType   : Tech                = Techs.get(base.getTech)
  def upgradingType : Upgrade             = Upgrades.get(base.getUpgrade)
  
  ////////////////
  // Visibility //
  ////////////////
  
  override def detected:Boolean = With.grids.enemyDetection.get(tileIncludingCenter)
  
  //////////////
  // Movement //
  //////////////
  
  //////////////
  // Statuses //
  //////////////
  
  def executionState:ExecutionState = With.executor.getState(this)
  
  def framesBeforeTechComplete      : Int = base.getRemainingResearchTime
  def framesBeforeUpgradeComplete   : Int = base.getRemainingUpgradeTime
  def framesBeforeBuildeeComplete   : Int = base.getRemainingTrainTime
  def framesBeforeBecomingComplete  : Int = base.getRemainingBuildTime
}
