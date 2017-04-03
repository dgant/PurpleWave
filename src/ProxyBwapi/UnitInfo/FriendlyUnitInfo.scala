package ProxyBwapi.UnitInfo

import ProxyBwapi.Techs.{Tech, Techs}
import ProxyBwapi.UnitClass.{UnitClass, UnitClasses}
import ProxyBwapi.Upgrades.{Upgrade, Upgrades}
import Startup.With

import scala.collection.JavaConverters._

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
  def trainingQueue : Iterable[UnitClass] = base.getTrainingQueue.asScala.map(UnitClasses.get)
  def teching       : Tech                = Techs.get(base.getTech)
  def upgrading     : Upgrade             = Upgrades.get(base.getUpgrade)
  
  ////////////////
  // Visibility //
  ////////////////
  
  override def detected = With.grids.enemyDetection.get(tileCenter)
  
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
