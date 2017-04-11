package ProxyBwapi.UnitInfo

import Lifecycle.With
import ProxyBwapi.Techs.{Tech, Techs}
import ProxyBwapi.Upgrades.{Upgrade, Upgrades}

class FriendlyUnitInfo(base:bwapi.Unit) extends FriendlyUnitProxy(base) {
  
  override def friendly: Option[FriendlyUnitInfo] = Some(this)
  
  ////////////
  // Health //
  ////////////
  
  ////////////
  // Combat //
  ////////////
  
  override def interceptors : Int = base.getInterceptorCount
  override def scarabs      : Int = base.getScarabCount
  
  //////////////
  // Geometry //
  //////////////
  
  ////////////
  // Orders //
  ////////////
  
  def getBuildUnit  : Option[UnitInfo]    = With.units.get(base.getBuildUnit)
  def teching       : Tech                = Techs.get(base.getTech)
  def upgrading     : Upgrade             = Upgrades.get(base.getUpgrade)
  
  ////////////////
  // Visibility //
  ////////////////
  
  override def detected = With.grids.enemyDetection.get(tileIncluding)
  
  //////////////
  // Movement //
  //////////////
  
  //////////////
  // Statuses //
  //////////////
  
  def framesBeforeTechComplete      : Int = base.getRemainingResearchTime
  def framesBeforeUpgradeComplete   : Int = base.getRemainingUpgradeTime
  def framesBeforeBuildeeComplete   : Int = base.getRemainingTrainTime
  def framesBeforeBecomingComplete  : Int = base.getRemainingBuildTime
}
