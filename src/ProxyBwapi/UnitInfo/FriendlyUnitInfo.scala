package ProxyBwapi.UnitInfo

import Lifecycle.With
import Micro.Agency.Agent
import Micro.Squads.Squad
import Performance.Cache
import ProxyBwapi.Techs.{Tech, Techs}
import ProxyBwapi.Upgrades.{Upgrade, Upgrades}

import scala.collection.JavaConverters._

class FriendlyUnitInfo(base: bwapi.Unit, id: Int) extends FriendlyUnitProxy(base, id) {
  
  override val friendly: Option[FriendlyUnitInfo] = Some(this)
  
  def squad: Option[Squad] = With.squads.squadByUnit.get(this)
  def squadmates: Seq[FriendlyUnitInfo] = squad.map(_.recruits).getOrElse(Seq.empty)
  def agent: Agent = With.agents.getState(this)
  def readyForMicro: Boolean = With.commander.ready(this)
  def teammates: Seq[UnitInfo] = teammatesCache()
  private val teammatesCache = new Cache(() => (squadmates ++ matchups.allies).distinct)
  
  ////////////
  // Health //
  ////////////
  
  //////////////
  // Geometry //
  //////////////
  
  ////////////
  // Orders //
  ////////////
  
  var lastSetRally: Int = 0
  
  def buildUnit  : Option[UnitInfo]  = With.units.get(base.getBuildUnit)
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
  
  def loadedUnits: Vector[FriendlyUnitInfo] = loadedUnitsCache()
  private val loadedUnitsCache = new Cache(() => base.getLoadedUnits.asScala.flatMap(With.units.get).flatMap(_.friendly).toVector)
  
  def transport: Option[FriendlyUnitInfo] = transportCache()
  //isBuilding check: Performance optimization
  private val transportCache = new Cache(() =>
    if (unitClass.isBuilding)
      None
    else
      With.units.get(base.getTransport).flatMap(_.friendly))
  
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
