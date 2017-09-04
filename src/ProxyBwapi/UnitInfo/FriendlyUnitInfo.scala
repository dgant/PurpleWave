package ProxyBwapi.UnitInfo

import Lifecycle.With
import Micro.Agency.Agent
import Micro.Squads.Squad
import Performance.CacheFrame
import ProxyBwapi.Techs.{Tech, Techs}
import ProxyBwapi.Upgrades.{Upgrade, Upgrades}

import scala.collection.JavaConverters._

class FriendlyUnitInfo(base: bwapi.Unit) extends FriendlyUnitProxy(base) {
  
  override def friendly: Option[FriendlyUnitInfo] = Some(this)
  
  def squad: Option[Squad] = With.squads.squadByUnit.get(this)
  def squadmates: Seq[FriendlyUnitInfo] = squad.map(_.recruits).getOrElse(Seq.empty)
  def agent: Agent = With.agents.getState(this)
  def readyForMicro: Boolean = With.commander.ready(this)
  
  ////////////
  // Health //
  ////////////
  
  //////////////
  // Geometry //
  //////////////
  
  ////////////
  // Orders //
  ////////////
  
  var hasSetRallyPoint: Boolean = false
  
  def getBuildUnit  : Option[UnitInfo]  = With.units.get(base.getBuildUnit)
  def techingType   : Tech              = Techs.get(base.getTech)
  def upgradingType : Upgrade           = Upgrades.get(base.getUpgrade)
  
  ////////////////
  // Visibility //
  ////////////////
  
  //////////////
  // Movement //
  //////////////
  
  //////////////
  // Statuses //
  //////////////
  
  def loadedUnits: Vector[FriendlyUnitInfo] = loadedUnitsCache.get
  private val loadedUnitsCache = new CacheFrame(() => base.getLoadedUnits.asScala.flatMap(With.units.get).flatMap(_.friendly).toVector)
  
  def transport: Option[FriendlyUnitInfo] = transportCache.get
  private val transportCache = new CacheFrame(() => With.units.get(base.getTransport).flatMap(_.friendly))
  
  def canTransport(passenger: FriendlyUnitInfo): Boolean =
    isTransport                       &&
    ! passenger.flying                &&
    ! passenger.unitClass.isBuilding  &&
    passenger.canMove                 &&
    passenger.transport.isEmpty       &&
    spaceRemaining >= passenger.unitClass.spaceRequired
  
  def framesBeforeTechComplete      : Int = base.getRemainingResearchTime
  def framesBeforeUpgradeComplete   : Int = base.getRemainingUpgradeTime
  def framesBeforeBuildeeComplete   : Int = base.getRemainingTrainTime
  def framesBeforeBecomingComplete  : Int = base.getRemainingBuildTime
}
