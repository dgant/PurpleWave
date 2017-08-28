package ProxyBwapi.UnitInfo
import Performance.Caching.{Cache, CacheFrame}
import ProxyBwapi.UnitClass.{UnitClass, UnitClasses}
import Lifecycle.With
import Mathematics.Points.{Pixel, Tile}
import ProxyBwapi.Players.{PlayerInfo, Players}
import ProxyBwapi.Races.Protoss
import bwapi._

import scala.collection.JavaConverters._

abstract class FriendlyUnitProxy(base: bwapi.Unit) extends UnitInfo(base) {
  
  override def equals(obj: Any): Boolean = obj.isInstanceOf[FriendlyUnitProxy] && obj.asInstanceOf[FriendlyUnitProxy].id == id
  override def hashCode(): Int = id.hashCode
  
  val cacheClass     = new Cache[UnitClass]     (5,  () =>  UnitClasses.get(base.getType))
  val cachePlayer    = new Cache[PlayerInfo]    (10, () =>  Players.get(base.getPlayer))
  val cachePixel     = new CacheFrame[Pixel]    (() =>  new Pixel(base.getPosition))
  val cacheTile      = new CacheFrame[Tile]     (() =>  new Tile(base.getTilePosition))
  val cacheCompleted = new CacheFrame[Boolean]  (() =>  base.isCompleted)
  val cacheExists    = new CacheFrame[Boolean]  (() =>  base.exists)
  val cacheSelected  = new CacheFrame[Boolean]  (() =>  base.isSelected)
  val cacheId        = new CacheFrame[Int]      (() =>  base.getID)
  val cachedFlying   = new CacheFrame[Boolean]  (() =>  base.isFlying)
  val cachedCloaked  = new CacheFrame[Boolean]  (() =>  base.isCloaked)
  val cachedStasised = new CacheFrame[Boolean]  (() =>  base.isStasised)
  
  ///////////////////
  // Tracking info //
  ///////////////////
  
  def player              : PlayerInfo  = cachePlayer.get
  def unitClass           : UnitClass   = cacheClass.get
  def lastSeen            : Int         = With.frame
  def possiblyStillThere  : Boolean     = alive
  
  def update(newBase: bwapi.Unit) {
    baseUnit = newBase
    updateHistory()
  }
  
  ////////////
  // Health //
  ////////////
  
  def alive                 : Boolean   = cacheExists.get
  def complete              : Boolean   = cacheCompleted.get
  def defensiveMatrixPoints : Int       = getDefenseMatrixPointsCache.get
  def hitPoints             : Int       = getHitPointsCache.get
  def initialResources      : Int       = getInitialResourcesCache.get
  def invincible            : Boolean   = isInvincibleCache.get
  def resourcesLeft         : Int       = getResourcesCache.get
  def shieldPoints          : Int       = getShieldsCache.get
  def energy                : Int       = getEnergyCache.get
  def plagued               : Boolean   = isPlaguedCache.get
  
  private val getDefenseMatrixPointsCache   = new CacheFrame(() => base.getDefenseMatrixPoints)
  private val getHitPointsCache             = new CacheFrame(() => base.getHitPoints)
  private val getInitialResourcesCache      = new CacheFrame(() => base.getInitialResources)
  private val isInvincibleCache             = new CacheFrame(() => base.isInvincible)
  private val getResourcesCache             = new CacheFrame(() => base.getResources)
  private val getShieldsCache               = new CacheFrame(() => base.getShields)
  private val getEnergyCache                = new CacheFrame(() => base.getEnergy)
  private val isPlaguedCache                = new CacheFrame(() => base.isPlagued)
  
  ////////////
  // Combat //
  ////////////
  
  def interceptors      : Iterable[UnitInfo] = if (is(Protoss.Carrier)) base.getInterceptors.asScala.flatMap(With.units.get) else List.empty
  def interceptorCount  : Int = interceptorCountCache.get
  def scarabCount       : Int = scarabCountCache.get
  
  def attackStarting            : Boolean = isStartingAttackCache.get
  def attackAnimationHappening  : Boolean = isAttackFrameCache.get
  def airCooldownLeft           : Int     = airCooldownLeftCache.get
  def groundCooldownLeft        : Int     = groundCooldownLeftCache.get
  def spellCooldownLeft         : Int     = spellCooldownLeftCache.get
  
  private val isStartingAttackCache   = new CacheFrame(() => base.isStartingAttack)
  private val isAttackFrameCache      = new CacheFrame(() => base.isAttackFrame)
  private val interceptorCountCache   = new CacheFrame(() => base.getInterceptorCount)
  private val scarabCountCache        = new CacheFrame(() => base.getScarabCount)
  private val airCooldownLeftCache    = new CacheFrame(() => base.getAirWeaponCooldown)
  private val groundCooldownLeftCache = new CacheFrame(() => base.getGroundWeaponCooldown)
  private val spellCooldownLeftCache  = new CacheFrame(() => base.getSpellCooldown)
  
  //////////////
  // Geometry //
  //////////////
  
  def pixelCenter : Pixel = cachePixel.get
  def tileTopLeft : Tile  = cacheTile.get
  def top         : Int   = base.getTop
  def left        : Int   = base.getLeft
  def right       : Int   = base.getRight
  def bottom      : Int   = base.getBottom
  
  ////////////
  // Orders //
  ////////////
  
  def gatheringMinerals : Boolean = isGatheringMineralsCache.get
  def gatheringGas      : Boolean = isGatheringGasCache.get
  
  private val isGatheringMineralsCache  = new CacheFrame(() => base.isGatheringMinerals)
  private val isGatheringGasCache       = new CacheFrame(() => base.isGatheringGas)
  
  private val badPositions = Vector(Position.Invalid, Position.None, Position.Unknown)
  
  def target            : Option[UnitInfo]  = targetCache.get
  def targetPixel       : Option[Pixel]     = targetPixelCache.get
  def order             : String            = base.getOrder.toString
  def orderTarget       : Option[UnitInfo]  = orderTargetCache.get
  def orderTargetPixel  : Option[Pixel]     = orderTargetPixelCache.get
  
  private val orderTargetCache      = new CacheFrame(() => { val target   = base.getOrderTarget;          if (target == null)                   None else With.units.getId(target.getID) })
  private val targetCache           = new CacheFrame(() => { val target   = base.getTarget;               if (target == null)                   None else With.units.getId(target.getID) })
  private val targetPixelCache      = new CacheFrame(() => { val position = base.getTargetPosition;       if (badPositions.contains(position))  None else Some(new Pixel(position)) })
  private val orderTargetPixelCache = new CacheFrame(() => { val position = base.getOrderTargetPosition;  if (badPositions.contains(position))  None else Some(new Pixel(position)) })
  
  def attacking       : Boolean = isAttackingCache.get
  def constructing    : Boolean = isConstructingCache.get
  def following       : Boolean = isFollowingCache.get
  def holdingPosition : Boolean = isHoldingPositionCache.get
  def idle            : Boolean = isIdleCache.get
  def interruptible   : Boolean = isInterruptibleCache.get
  def morphing        : Boolean = isMorphingCache.get
  def repairing       : Boolean = isRepairingCache.get
  def teching     : Boolean = isResearchingCache.get
  def patrolling      : Boolean = isPatrollingCache.get
  def training        : Boolean = isTrainingCache.get
  def upgrading       : Boolean = isUpgradingCache.get
  
  private val isAttackingCache        = new CacheFrame(() => base.isAttacking)
  private val isConstructingCache     = new CacheFrame(() => base.isConstructing)
  private val isFollowingCache        = new CacheFrame(() => base.isFollowing)
  private val isHoldingPositionCache  = new CacheFrame(() => base.isHoldingPosition)
  private val isIdleCache             = new CacheFrame(() => base.isIdle)
  private val isInterruptibleCache    = new CacheFrame(() => base.isInterruptible)
  private val isMorphingCache         = new CacheFrame(() => base.isMorphing)
  private val isRepairingCache        = new CacheFrame(() => base.isRepairing)
  private val isResearchingCache      = new CacheFrame(() => base.isResearching)
  private val isPatrollingCache       = new CacheFrame(() => base.isPatrolling)
  private val isTrainingCache         = new CacheFrame(() => base.isTraining)
  private val isUpgradingCache        = new CacheFrame(() => base.isUpgrading)
  
  def command: Option[UnitCommand]  = getLastCommandCache.get
  def commandFrame: Int          = getLastCommandFrameCache.get
  
  private val getLastCommandCache      = new CacheFrame(() => Option(base.getLastCommand) )
  private val getLastCommandFrameCache = new CacheFrame(() => base.getLastCommandFrame)
  
  def trainingQueue: Iterable[UnitClass] = trainingQueueCache.get
  
  private val trainingQueueCache = new CacheFrame(() => base.getTrainingQueue.asScala.map(UnitClasses.get))
  
  ////////////////
  // Visibility //
  ////////////////
  
  def burrowed  : Boolean = isBurrowedCache.get
  def cloaked   : Boolean = cachedCloaked.get
  def detected  : Boolean = base.isDetected
  def visible   : Boolean = isVisibleCache.get
  
  private val isBurrowedCache = new CacheFrame(() => base.isBurrowed)
  private val isVisibleCache = new CacheFrame(() => base.isVisible)
  
  //////////////
  // Movement //
  //////////////
  
  def accelerating  : Boolean = base.isAccelerating
  def angleRadians  : Double  = base.getAngle
  def braking       : Boolean = base.isBraking
  def ensnared      : Boolean = base.isEnsnared
  def flying        : Boolean = cachedFlying.get
  def lifted        : Boolean = base.isLifted
  def lockedDown    : Boolean = cachedIsLockedDown.get
  def maelstrommed  : Boolean = cachedIsMaelstrommed.get
  def sieged        : Boolean = base.isSieged
  def stasised      : Boolean = cachedStasised.get
  def stimmed       : Boolean = cachedIsStimmed.get
  def stuck         : Boolean = base.isStuck
  def velocityX     : Double  = base.getVelocityX
  def velocityY     : Double  = base.getVelocityY
  
  private val cachedIsStimmed = new CacheFrame(() => base.isStimmed)
  private val cachedIsLockedDown = new CacheFrame(() => base.isLockedDown)
  private val cachedIsMaelstrommed = new CacheFrame(() => base.isMaelstrommed)
  
  //////////////
  // Statuses //
  //////////////
  
  def remainingBuildFrames    : Int = base.getRemainingBuildTime
  def remainingUpgradeFrames  : Int = base.getRemainingUpgradeTime
  def remainingTechFrames     : Int = base.getRemainingResearchTime
  
  def beingConstructed    : Boolean = base.isBeingConstructed
  def beingGathered       : Boolean = base.isBeingGathered
  def beingHealed         : Boolean = base.isBeingHealed
  def blind               : Boolean = base.isBlind
  def carryingMinerals    : Boolean = carryingMineralsCache.get
  def carryingGas         : Boolean = carryingGasCache.get
  def powered             : Boolean = base.isPowered
  def selected            : Boolean = cacheSelected.get
  def targetable          : Boolean = base.isTargetable
  def underAttack         : Boolean = base.isUnderAttack
  def underDarkSwarm      : Boolean = base.isUnderDarkSwarm
  def underDisruptionWeb  : Boolean = base.isUnderDisruptionWeb
  def underStorm          : Boolean = base.isUnderStorm
  
  private val carryingMineralsCache = new CacheFrame(() => base.isCarryingMinerals)
  private val carryingGasCache      = new CacheFrame(() => base.isCarryingGas)
  
  def spiderMines: Int = base.getSpiderMineCount
  
  def addon: Option[UnitInfo] = With.units.get(base.getAddon)
  
  def spaceRemaining: Int = spaceRemainingCache.get
  private val spaceRemainingCache = new CacheFrame(() => base.getSpaceRemaining)
}
