package ProxyBwapi.UnitInfo
import Lifecycle.With
import Mathematics.Points.{Pixel, Tile}
import Performance.Cache
import ProxyBwapi.ConvertBWAPI
import ProxyBwapi.Players.{PlayerInfo, Players}
import ProxyBwapi.Techs.Tech
import ProxyBwapi.UnitClasses.{UnitClass, UnitClasses}
import ProxyBwapi.Upgrades.Upgrade

import scala.collection.JavaConverters._

abstract class FriendlyUnitProxy(originalBwapiUnit: bwapi.Unit, id: Int) extends UnitInfo(originalBwapiUnit, id) {

  override def equals(obj: Any): Boolean = obj.isInstanceOf[FriendlyUnitProxy] && obj.asInstanceOf[FriendlyUnitProxy].id == id

  private val cacheClass     = new Cache[UnitClass]  (() =>  UnitClasses.get(bwapiUnit.getType))
  private val cachePlayer    = new Cache[PlayerInfo] (() =>  Players.get(bwapiUnit.getPlayer))
  private val cachePixel     = new Cache[Pixel]      (() =>  new Pixel(bwapiUnit.getPosition))
  private val cacheTile      = new Cache[Tile]       (() =>  new Tile(bwapiUnit.getTilePosition))
  private val cacheExists    = new Cache[Boolean]    (() =>  bwapiUnit.exists)

  ///////////////////
  // Tracking info //
  ///////////////////

  def player    : PlayerInfo  = cachePlayer()
  def unitClass : UnitClass   = cacheClass()
  def lastSeen  : Int         = With.frame

  ////////////
  // Health //
  ////////////

  def alive                 : Boolean   = bwapiUnit.exists
  def complete              : Boolean   = bwapiUnit.isCompleted
  def defensiveMatrixPoints : Int       = bwapiUnit.getDefenseMatrixPoints
  def hitPoints             : Int       = bwapiUnit.getHitPoints
  def initialResources      : Int       = bwapiUnit.getInitialResources
  def invincible            : Boolean   = bwapiUnit.isInvincible
  def resourcesLeft         : Int       = bwapiUnit.getResources
  def shieldPoints          : Int       = bwapiUnit.getShields
  def energy                : Int       = bwapiUnit.getEnergy
  def plagued               : Boolean   = bwapiUnit.isPlagued

  ////////////
  // Combat //
  ////////////

  def interceptors: Iterable[UnitInfo] = interceptorsCache()
  def scarabCount: Int = bwapiUnit.getScarabCount

  private val interceptorsCache = new Cache(() => bwapiUnit.getInterceptors.asScala.flatMap(With.units.get))

  def airCooldownLeft           : Int     = bwapiUnit.getAirWeaponCooldown
  def groundCooldownLeft        : Int     = bwapiUnit.getGroundWeaponCooldown
  def spellCooldownLeft         : Int     = bwapiUnit.getSpellCooldown

  //////////////
  // Geometry //
  //////////////

  def pixel : Pixel = cachePixel()
  def tileTopLeft : Tile  = cacheTile()

  ////////////
  // Orders //
  ////////////

  def gatheringMinerals : Boolean = bwapiUnit.isGatheringMinerals
  def gatheringGas      : Boolean = bwapiUnit.isGatheringGas

  def target            : Option[UnitInfo]  = targetCache()
  def targetPixel       : Option[Pixel]     = targetPixelCache()
  def order             : String            = bwapiUnit.getOrder.toString
  def orderTarget       : Option[UnitInfo]  = orderTargetCache()
  def orderTargetPixel  : Option[Pixel]     = orderTargetPixelCache()

  private val orderTargetCache      = new Cache(() => With.units.get(bwapiUnit.getOrderTarget))
  private val orderTargetPixelCache = new Cache(() => ConvertBWAPI.position(bwapiUnit.getOrderTargetPosition))
  private val targetCache           = new Cache(() => With.units.get(bwapiUnit.getTarget))
  private val targetPixelCache      = new Cache(() => ConvertBWAPI.position(bwapiUnit.getTargetPosition))

  def attacking       : Boolean = bwapiUnit.isAttacking
  def constructing    : Boolean = bwapiUnit.isConstructing
  def morphing        : Boolean = bwapiUnit.isMorphing
  def repairing       : Boolean = bwapiUnit.isRepairing
  def teching         : Boolean = bwapiUnit.isResearching
  def upgrading       : Boolean = bwapiUnit.isUpgrading
  def training        : Boolean = bwapiUnit.isTraining

  def buildType: UnitClass = buildTypeCache()
  def trainingQueue: Iterable[UnitClass] = trainingQueueCache()
  def techProducing: Option[Tech] = techProducingCache()
  def upgradeProducing: Option[Upgrade] = upgradeProducingCache()
  def unitProducing: Option[UnitClass] = trainingQueue.headOption

  private val buildTypeCache = new Cache(() => UnitClasses.get(bwapiUnit.getBuildType))
  private val trainingQueueCache = new Cache(() => if (unitClass.trainsUnits) bwapiUnit.getTrainingQueue.asScala.map(UnitClasses.get) else Iterable.empty)
  private val techProducingCache = new Cache(() => ConvertBWAPI.tech(bwapiUnit.getTech))
  private val upgradeProducingCache = new Cache(() => ConvertBWAPI.upgrade(bwapiUnit.getUpgrade))

  ////////////////
  // Visibility //
  ////////////////

  def burrowed  : Boolean = bwapiUnit.isBurrowed
  def cloaked   : Boolean = bwapiUnit.isCloaked
  def detected  : Boolean = bwapiUnit.isDetected
  def visible   : Boolean = bwapiUnit.isVisible

  //////////////
  // Movement //
  //////////////

  def accelerating  : Boolean = bwapiUnit.isAccelerating
  def angleRadians  : Double  = bwapiUnit.getAngle
  def braking       : Boolean = bwapiUnit.isBraking
  def ensnared      : Boolean = bwapiUnit.isEnsnared
  def flying        : Boolean = bwapiUnit.isFlying
  def irradiated    : Boolean = bwapiUnit.isIrradiated
  def lifted        : Boolean = bwapiUnit.isLifted
  def loaded        : Boolean = bwapiUnit.isLoaded
  def lockedDown    : Boolean = bwapiUnit.isLockedDown
  def maelstrommed  : Boolean = bwapiUnit.isMaelstrommed
  def sieged        : Boolean = bwapiUnit.isSieged
  def stasised      : Boolean = bwapiUnit.isStasised
  def stimmed       : Boolean = bwapiUnit.isStimmed
  def stuck         : Boolean = bwapiUnit.isStuck
  def velocityX     : Double  = bwapiUnit.getVelocityX
  def velocityY     : Double  = bwapiUnit.getVelocityY

  //////////////
  // Statuses //
  //////////////

  def remainingCompletionFrames : Int = bwapiUnit.getRemainingBuildTime
  def remainingUpgradeFrames    : Int = bwapiUnit.getRemainingUpgradeTime
  def remainingTechFrames       : Int = bwapiUnit.getRemainingResearchTime
  def remainingTrainFrames      : Int = bwapiUnit.getRemainingTrainTime

  def beingConstructed    : Boolean = bwapiUnit.isBeingConstructed
  def beingGathered       : Boolean = bwapiUnit.isBeingGathered
  def beingHealed         : Boolean = bwapiUnit.isBeingHealed
  def blind               : Boolean = bwapiUnit.isBlind
  def carryingMinerals    : Boolean = bwapiUnit.isCarryingMinerals
  def carryingGas         : Boolean = bwapiUnit.isCarryingGas
  def powered             : Boolean = bwapiUnit.isPowered
  def selected            : Boolean = bwapiUnit.isSelected
  def underAttack         : Boolean = bwapiUnit.isUnderAttack
  def underDarkSwarm      : Boolean = bwapiUnit.isUnderDarkSwarm
  def underDisruptionWeb  : Boolean = bwapiUnit.isUnderDisruptionWeb
  def underStorm          : Boolean = bwapiUnit.isUnderStorm

  def spiderMines: Int = bwapiUnit.getSpiderMineCount

  def addon: Option[UnitInfo] = With.units.get(bwapiUnit.getAddon)

  def hasNuke: Boolean = bwapiUnit.hasNuke

  def framesUntilRemoval: Int = bwapiUnit.getRemoveTimer

  def spaceRemaining: Int = bwapiUnit.getSpaceRemaining

  def kills: Int = bwapiUnit.getKillCount
}
