package ProxyBwapi.UnitInfo
import Lifecycle.With
import Mathematics.Points.{Pixel, Tile}
import Performance.Cache
import ProxyBwapi.Players.{PlayerInfo, Players}
import ProxyBwapi.Races.Protoss
import ProxyBwapi.UnitClass.{UnitClass, UnitClasses}
import bwapi._

import scala.collection.JavaConverters._

abstract class FriendlyUnitProxy(base: bwapi.Unit, id: Int) extends UnitInfo(base, id) {
  
  override def equals(obj: Any): Boolean = obj.isInstanceOf[FriendlyUnitProxy] && obj.asInstanceOf[FriendlyUnitProxy].id == id
  override def hashCode(): Int = id.hashCode
  
  val cacheClass     = new Cache[UnitClass]  (() =>  UnitClasses.get(base.getType))
  val cachePlayer    = new Cache[PlayerInfo] (() =>  Players.get(base.getPlayer))
  val cachePixel     = new Cache[Pixel]      (() =>  new Pixel(base.getPosition))
  val cacheTile      = new Cache[Tile]       (() =>  new Tile(base.getTilePosition))
  val cacheCompleted = new Cache[Boolean]    (() =>  base.isCompleted)
  val cacheExists    = new Cache[Boolean]    (() =>  base.exists)
  val cacheSelected  = new Cache[Boolean]    (() =>  base.isSelected)
  val cachedFlying   = new Cache[Boolean]    (() =>  base.isFlying)
  val cachedCloaked  = new Cache[Boolean]    (() =>  base.isCloaked)
  val cachedStasised = new Cache[Boolean]    (() =>  base.isStasised)
  
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
  
  def alive                 : Boolean   = cacheExists()
  def complete              : Boolean   = cacheCompleted()
  def defensiveMatrixPoints : Int       = getDefenseMatrixPointsCache()
  def hitPoints             : Int       = getHitPointsCache()
  def initialResources      : Int       = getInitialResourcesCache()
  def invincible            : Boolean   = isInvincibleCache()
  def resourcesLeft         : Int       = getResourcesCache()
  def shieldPoints          : Int       = getShieldsCache()
  def energy                : Int       = getEnergyCache()
  def plagued               : Boolean   = isPlaguedCache()
  
  private val getDefenseMatrixPointsCache   = new Cache(() => base.getDefenseMatrixPoints)
  private val getHitPointsCache             = new Cache(() => base.getHitPoints)
  private val getInitialResourcesCache      = new Cache(() => base.getInitialResources)
  private val isInvincibleCache             = new Cache(() => base.isInvincible)
  private val getResourcesCache             = new Cache(() => base.getResources)
  private val getShieldsCache               = new Cache(() => base.getShields)
  private val getEnergyCache                = new Cache(() => base.getEnergy)
  private val isPlaguedCache                = new Cache(() => base.isPlagued)
  
  ////////////
  // Combat //
  ////////////
  
  def interceptors      : Iterable[UnitInfo] = if (is(Protoss.Carrier)) base.getInterceptors.asScala.flatMap(With.units.get) else List.empty
  def interceptorCount  : Int = interceptorCountCache()
  def scarabCount       : Int = scarabCountCache()
  
  def attackStarting            : Boolean = isStartingAttackCache()
  def attackAnimationHappening  : Boolean = isAttackFrameCache()
  def airCooldownLeft           : Int     = airCooldownLeftCache()
  def groundCooldownLeft        : Int     = groundCooldownLeftCache()
  def spellCooldownLeft         : Int     = spellCooldownLeftCache()
  
  private val isStartingAttackCache   = new Cache(() => base.isStartingAttack)
  private val isAttackFrameCache      = new Cache(() => base.isAttackFrame)
  private val interceptorCountCache   = new Cache(() => base.getInterceptorCount)
  private val scarabCountCache        = new Cache(() => base.getScarabCount)
  private val airCooldownLeftCache    = new Cache(() => base.getAirWeaponCooldown)
  private val groundCooldownLeftCache = new Cache(() => base.getGroundWeaponCooldown)
  private val spellCooldownLeftCache  = new Cache(() => base.getSpellCooldown)
  
  //////////////
  // Geometry //
  //////////////
  
  def pixelCenter : Pixel = cachePixel()
  def tileTopLeft : Tile  = cacheTile()
  def top         : Int   = base.getTop
  def left        : Int   = base.getLeft
  def right       : Int   = base.getRight
  def bottom      : Int   = base.getBottom
  
  ////////////
  // Orders //
  ////////////
  
  def gatheringMinerals : Boolean = isGatheringMineralsCache()
  def gatheringGas      : Boolean = isGatheringGasCache()
  
  private val isGatheringMineralsCache  = new Cache(() => base.isGatheringMinerals)
  private val isGatheringGasCache       = new Cache(() => base.isGatheringGas)
  
  private val badPositions = Vector(Position.Invalid, Position.None, Position.Unknown)
  
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
  
  def attacking       : Boolean = isAttackingCache()
  def constructing    : Boolean = isConstructingCache()
  def following       : Boolean = isFollowingCache()
  def holdingPosition : Boolean = isHoldingPositionCache()
  def idle            : Boolean = isIdleCache()
  def interruptible   : Boolean = isInterruptibleCache()
  def morphing        : Boolean = isMorphingCache()
  def repairing       : Boolean = isRepairingCache()
  def teching         : Boolean = isResearchingCache()
  def patrolling      : Boolean = isPatrollingCache()
  def training        : Boolean = isTrainingCache()
  def upgrading       : Boolean = isUpgradingCache()
  
  private val isAttackingCache        = new Cache(() => base.isAttacking)
  private val isConstructingCache     = new Cache(() => base.isConstructing)
  private val isFollowingCache        = new Cache(() => base.isFollowing)
  private val isHoldingPositionCache  = new Cache(() => base.isHoldingPosition)
  private val isIdleCache             = new Cache(() => base.isIdle)
  private val isInterruptibleCache    = new Cache(() => base.isInterruptible)
  private val isMorphingCache         = new Cache(() => base.isMorphing)
  private val isRepairingCache        = new Cache(() => base.isRepairing)
  private val isResearchingCache      = new Cache(() => base.isResearching)
  private val isPatrollingCache       = new Cache(() => base.isPatrolling)
  private val isTrainingCache         = new Cache(() => base.isTraining)
  private val isUpgradingCache        = new Cache(() => base.isUpgrading)
  
  def command: Option[UnitCommand]  = getLastCommandCache()
  def commandFrame: Int          = getLastCommandFrameCache()
  
  private val getLastCommandCache      = new Cache(() => Option(base.getLastCommand) )
  private val getLastCommandFrameCache = new Cache(() => base.getLastCommandFrame)
  
  def trainingQueue: Iterable[UnitClass] = trainingQueueCache()
  
  private val trainingQueueCache = new Cache(() =>
    if (unitClass.isBuilding || is(Protoss.Reaver) || is(Protoss.Carrier))
      base.getTrainingQueue.asScala.map(UnitClasses.get)
    else
      Iterable.empty // Performance optimization
  )
  
  ////////////////
  // Visibility //
  ////////////////
  
  def burrowed  : Boolean = isBurrowedCache()
  def cloaked   : Boolean = cachedCloaked()
  def detected  : Boolean = base.isDetected
  def visible   : Boolean = isVisibleCache()
  
  private val isBurrowedCache = new Cache(() => base.isBurrowed)
  private val isVisibleCache = new Cache(() => base.isVisible)
  
  //////////////
  // Movement //
  //////////////
  
  def accelerating  : Boolean = base.isAccelerating
  def angleRadians  : Double  = base.getAngle
  def braking       : Boolean = base.isBraking
  def ensnared      : Boolean = base.isEnsnared
  def flying        : Boolean = cachedFlying()
  def lifted        : Boolean = base.isLifted
  def lockedDown    : Boolean = cachedIsLockedDown()
  def maelstrommed  : Boolean = cachedIsMaelstrommed()
  def sieged        : Boolean = base.isSieged
  def stasised      : Boolean = cachedStasised()
  def stimmed       : Boolean = cachedIsStimmed()
  def stuck         : Boolean = base.isStuck
  def velocityX     : Double  = base.getVelocityX
  def velocityY     : Double  = base.getVelocityY
  
  private val cachedIsStimmed = new Cache(() => base.isStimmed)
  private val cachedIsLockedDown = new Cache(() => base.isLockedDown)
  private val cachedIsMaelstrommed = new Cache(() => base.isMaelstrommed)
  
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
  def carryingMinerals    : Boolean = carryingMineralsCache()
  def carryingGas         : Boolean = carryingGasCache()
  def powered             : Boolean = base.isPowered
  def selected            : Boolean = cacheSelected()
  def targetable          : Boolean = base.isTargetable
  def underAttack         : Boolean = base.isUnderAttack
  def underDarkSwarm      : Boolean = base.isUnderDarkSwarm
  def underDisruptionWeb  : Boolean = base.isUnderDisruptionWeb
  def underStorm          : Boolean = base.isUnderStorm
  
  private val carryingMineralsCache = new Cache(() => base.isCarryingMinerals)
  private val carryingGasCache      = new Cache(() => base.isCarryingGas)
  
  def spiderMines: Int = base.getSpiderMineCount
  
  def addon: Option[UnitInfo] = cachedAddon()
  private val cachedAddon = new Cache(() => With.units.get(base.getAddon))
  
  def hasNuke: Boolean = cachedHasNuke()
  private val cachedHasNuke = new Cache(() => base.hasNuke)
  
  def spaceRemaining: Int = spaceRemainingCache()
  private val spaceRemainingCache = new Cache(() => base.getSpaceRemaining)
}
