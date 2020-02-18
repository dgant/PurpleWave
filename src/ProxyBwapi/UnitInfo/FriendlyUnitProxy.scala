package ProxyBwapi.UnitInfo
import Lifecycle.With
import Mathematics.Points.{Pixel, Tile}
import Performance.Cache
import ProxyBwapi.Players.{PlayerInfo, Players}
import ProxyBwapi.Techs.{Tech, Techs}
import ProxyBwapi.UnitClasses.{UnitClass, UnitClasses}
import ProxyBwapi.Upgrades.{Upgrade, Upgrades}
import bwapi._

import scala.collection.JavaConverters._

abstract class FriendlyUnitProxy(base: bwapi.Unit, id: Int) extends UnitInfo(base, id) {
  
  override def equals(obj: Any): Boolean = obj.isInstanceOf[FriendlyUnitProxy] && obj.asInstanceOf[FriendlyUnitProxy].id == id
  override def hashCode(): Int = id.hashCode
  
  private val cacheClass     = new Cache[UnitClass]  (() =>  UnitClasses.get(base.getType))
  private val cachePlayer    = new Cache[PlayerInfo] (() =>  Players.get(base.getPlayer))
  private val cachePixel     = new Cache[Pixel]      (() =>  new Pixel(base.getPosition))
  private val cacheTile      = new Cache[Tile]       (() =>  new Tile(base.getTilePosition))
  private val cacheExists    = new Cache[Boolean]    (() =>  { val e = base.exists; With.performance.trackUnit(id, e); e })
  
  ///////////////////
  // Tracking info //
  ///////////////////
  
  def player              : PlayerInfo  = cachePlayer()
  def unitClass           : UnitClass   = cacheClass()
  def lastSeen            : Int         = With.frame
  def possiblyStillThere  : Boolean     = alive
  
  def update(newBase: bwapi.Unit) {
    baseUnit = newBase
    updateCommon()
  }
  
  ////////////
  // Health //
  ////////////
  
  def alive                 : Boolean   = { val e = base.exists; With.performance.trackUnit(id, e); e }
  def complete              : Boolean   = base.isCompleted
  def defensiveMatrixPoints : Int       = base.getDefenseMatrixPoints
  def hitPoints             : Int       = base.getHitPoints
  def initialResources      : Int       = base.getInitialResources
  def invincible            : Boolean   = base.isInvincible
  def resourcesLeft         : Int       = base.getResources
  def shieldPoints          : Int       = base.getShields
  def energy                : Int       = base.getEnergy
  def plagued               : Boolean   = base.isPlagued
  
  ////////////
  // Combat //
  ////////////
  
  def interceptors      : Iterable[UnitInfo] = interceptorsCache()
  def interceptorCount  : Int = base.getInterceptorCount
  def scarabCount       : Int = base.getScarabCount
  
  private val interceptorsCache = new Cache(() => base.getInterceptors.asScala.flatMap(With.units.get))
  
  def attackStarting            : Boolean = base.isStartingAttack
  def attackAnimationHappening  : Boolean = base.isAttackFrame
  def airCooldownLeft           : Int     = base.getAirWeaponCooldown
  def groundCooldownLeft        : Int     = base.getGroundWeaponCooldown
  def spellCooldownLeft         : Int     = base.getSpellCooldown
  
  //////////////
  // Geometry //
  //////////////
  
  def pixelCenter : Pixel = cachePixel()
  def tileTopLeft : Tile  = cacheTile()
  
  ////////////
  // Orders //
  ////////////
  
  def gatheringMinerals : Boolean = base.isGatheringMinerals
  def gatheringGas      : Boolean = base.isGatheringGas
  
  private val badPositions = Vector(null, Position.Invalid, Position.None, Position.Unknown)
  
  def target            : Option[UnitInfo]  = targetCache()
  def targetPixel       : Option[Pixel]     = targetPixelCache()
  def order             : String            = base.getOrder.toString
  def orderTarget       : Option[UnitInfo]  = orderTargetCache()
  def orderTargetPixel  : Option[Pixel]     = orderTargetPixelCache()
  
  private val orderTargetCache = new Cache(() => {
    val target = if (unitClass.targetsMatter) base.getOrderTarget else null
    if (target == null) None else With.units.get(target)
  })
  private val orderTargetPixelCache = new Cache(() => {
    val position = if (unitClass.targetPositionsMatter) base.getOrderTargetPosition else null
    if (badPositions.contains(position)) None else Some(new Pixel(position))
  })
  private val targetCache = new Cache(() => {
    val target = if (unitClass.targetsMatter) base.getTarget else null
    if (target == null) None else With.units.get(target)
  })
  private val targetPixelCache = new Cache(() => {
    val position = if (unitClass.targetPositionsMatter) base.getTargetPosition else null
    if (badPositions.contains(position)) None else Some(new Pixel(position))
  })
  
  def attacking       : Boolean = base.isAttacking
  def constructing    : Boolean = base.isConstructing
  def following       : Boolean = base.isFollowing
  def holdingPosition : Boolean = base.isHoldingPosition
  def idle            : Boolean = base.isIdle
  def interruptible   : Boolean = base.isInterruptible
  def morphing        : Boolean = base.isMorphing
  def repairing       : Boolean = base.isRepairing
  def teching         : Boolean = base.isResearching
  def upgrading       : Boolean = base.isUpgrading
  def patrolling      : Boolean = base.isPatrolling
  def training        : Boolean = base.isTraining
  
  def command: Option[UnitCommand]  = getLastCommandCache()
  def commandFrame: Int             = base.getLastCommandFrame
  
  private val getLastCommandCache = new Cache(() => Option(base.getLastCommand) )
  
  def buildType: UnitClass = buildTypeCache()
  def trainingQueue: Iterable[UnitClass] = trainingQueueCache()
  def techProducing: Option[Tech] = techProducingCache()
  def upgradeProducing: Option[Upgrade] = upgradeProducingCache()
  def unitProducing: Option[UnitClass] = trainingQueue.headOption
  
  private val buildTypeCache = new Cache(() => UnitClasses.get(base.getBuildType))
  private val trainingQueueCache = new Cache(() =>
    if (unitClass.trainsUnits)
      base.getTrainingQueue.asScala.map(UnitClasses.get)
    else
      Iterable.empty // Performance optimization
  )
  private val techProducingCache = new Cache[Option[Tech]](() =>
    if (unitClass.techsWhat.nonEmpty && teching)
      Some(Techs.get(baseUnit.getTech)).filter(x => x != Techs.None && x != Techs.Unknown)
    else None
  )
  private val upgradeProducingCache = new Cache[Option[Upgrade]](() =>
    if (unitClass.upgradesWhat.nonEmpty && upgrading)
      Some(Upgrades.get(baseUnit.getUpgrade)).filter(x => x != Upgrades.None && x != Upgrades.Unknown)
    else None
  )
  
  ////////////////
  // Visibility //
  ////////////////
  
  def burrowed  : Boolean = base.isBurrowed
  def cloaked   : Boolean = base.isCloaked
  def detected  : Boolean = base.isDetected
  def visible   : Boolean = base.isVisible
  
  //////////////
  // Movement //
  //////////////
  
  def accelerating  : Boolean = base.isAccelerating
  def angleRadians  : Double  = base.getAngle
  def braking       : Boolean = base.isBraking
  def ensnared      : Boolean = base.isEnsnared
  def flying        : Boolean = base.isFlying
  def irradiated    : Boolean = base.isIrradiated
  def lifted        : Boolean = base.isLifted
  def loaded        : Boolean = base.isLoaded
  def lockedDown    : Boolean = base.isLockedDown
  def maelstrommed  : Boolean = base.isMaelstrommed
  def sieged        : Boolean = base.isSieged
  def stasised      : Boolean = base.isStasised
  def stimmed       : Boolean = base.isStimmed
  def stuck         : Boolean = base.isStuck
  def velocityX     : Double  = base.getVelocityX
  def velocityY     : Double  = base.getVelocityY

  //////////////
  // Statuses //
  //////////////
  
  def remainingCompletionFrames : Int = base.getRemainingTrainTime
  def remainingUpgradeFrames    : Int = if (complete) 0 else base.getRemainingBuildTime
  def remainingTechFrames       : Int = base.getRemainingUpgradeTime
  def remainingTrainFrames      : Int = base.getRemainingResearchTime
  
  def beingConstructed    : Boolean = base.isBeingConstructed
  def beingGathered       : Boolean = base.isBeingGathered
  def beingHealed         : Boolean = base.isBeingHealed
  def blind               : Boolean = base.isBlind
  def carryingMinerals    : Boolean = base.isCarryingMinerals
  def carryingGas         : Boolean = base.isCarryingGas
  def powered             : Boolean = base.isPowered
  def selected            : Boolean = base.isSelected
  def targetable          : Boolean = base.isTargetable
  def underAttack         : Boolean = base.isUnderAttack
  def underDarkSwarm      : Boolean = base.isUnderDarkSwarm
  def underDisruptionWeb  : Boolean = base.isUnderDisruptionWeb
  def underStorm          : Boolean = base.isUnderStorm
  
  def spiderMines: Int = base.getSpiderMineCount
  
  def addon: Option[UnitInfo] = cachedAddon()
  private val cachedAddon = new Cache(() =>
    if (unitClass.isTerran
    && unitClass.isBuilding
    && (unitClass.isTownHall || unitClass.isFactory || unitClass.isStarport || unitClass.isScienceFacility))
    With.units.get(base.getAddon) else None)
  
  def hasNuke: Boolean = base.hasNuke
  
  def spaceRemaining: Int = base.getSpaceRemaining
  
  def kills: Int = base.getKillCount
}
