package ProxyBwapi.UnitInfo

import ProxyBwapi.Techs.{Tech, Techs}
import ProxyBwapi.Upgrades.{Upgrade, Upgrades}
import Startup.With
import bwapi._

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
  
  def cooldownLeft : Int      = Math.max(base.getGroundWeaponCooldown, base.getAirWeaponCooldown)
  def onCooldown   : Boolean  = cooldownLeft > 0 || ! unitClass.canAttack
  
  //////////////
  // Geometry //
  //////////////
  
  ////////////
  // Orders //
  ////////////
  
  def getBuildUnit  : Option[UnitInfo]   = With.units.get(base.getBuildUnit)
  def trainingQueue : Iterable[UnitType] = base.getTrainingQueue.asScala
  def teching       : Tech               = Techs.get(base.getTech)
  def upgrading     : Upgrade            = Upgrades.get(base.getUpgrade)
  
  ////////////////
  // Visibility //
  ////////////////
  
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
