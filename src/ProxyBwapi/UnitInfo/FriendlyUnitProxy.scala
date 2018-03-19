package ProxyBwapi.UnitInfo
import Information.Intelligenze.Fingerprinting.Generic.GameTime
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
  
  private val cacheClass     = new Cache[UnitClass]  (() =>  UnitClasses.get(base.getType), GameTime(0, 1)())
  private val cachePlayer    = new Cache[PlayerInfo] (() =>  Players.get(base.getPlayer))
  private val cachePixel     = new Cache[Pixel]      (() =>  new Pixel(base.getPosition))
  private val cacheTile      = new Cache[Tile]       (() =>  new Tile(base.getTilePosition))
  private val cacheCompleted = new Cache[Boolean]    (() =>  base.isCompleted)
  private val cacheExists    = new Cache[Boolean]    (() =>  base.exists)
  private val cacheSelected  = new Cache[Boolean]    (() =>  base.isSelected)
  
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
  
  private val isStartingAttackCache   = new Cache(() => unitClass.rawCanAttack && base.isStartingAttack)
  private val isAttackFrameCache      = new Cache(() => unitClass.rawCanAttack && base.isAttackFrame)
  private val interceptorCountCache   = new Cache(() => if (is(Protoss.Carrier)) base.getInterceptorCount else 0)
  private val scarabCountCache        = new Cache(() => if (is(Protoss.Reaver)) base.getScarabCount else 0)
  private val airCooldownLeftCache    = new Cache(() => base.getAirWeaponCooldown)
  private val groundCooldownLeftCache = new Cache(() => base.getGroundWeaponCooldown)
  private val spellCooldownLeftCache  = new Cache(() => base.getSpellCooldown)
  
  //////////////
  // Geometry //
  //////////////
  
  def pixelCenter : Pixel = cachePixel()
  def tileTopLeft : Tile  = cacheTile()
  def top         : Int   = cacheTop()
  def left        : Int   = cacheLeft()
  def right       : Int   = cacheRight()
  def bottom      : Int   = cacheBottom()
  
  private val useRealDimensions = ! isInterceptor() && ! isSpiderMine()
  private val cacheTop    = new Cache(() => if (useRealDimensions) base.getTop    else pixelCenter.y - unitClass.height / 2)
  private val cacheLeft   = new Cache(() => if (useRealDimensions) base.getLeft   else pixelCenter.x - unitClass.width  / 2)
  private val cacheRight  = new Cache(() => if (useRealDimensions) base.getRight  else pixelCenter.x + unitClass.width  / 2)
  private val cacheBottom = new Cache(() => if (useRealDimensions) base.getBottom else pixelCenter.y + unitClass.height / 2)
  
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
  
  def buildType: UnitClass = buildTypeCache()
  def trainingQueue: Iterable[UnitClass] = trainingQueueCache()
  
  private val buildTypeCache = new Cache(() => UnitClasses.get(base.getBuildType))
  private val trainingQueueCache = new Cache(() =>
    if (unitClass.trainsUnits)
      base.getTrainingQueue.asScala.map(UnitClasses.get)
    else
      Iterable.empty // Performance optimization
  )
  
  ////////////////
  // Visibility //
  ////////////////
  
  def burrowed  : Boolean = unitClass.canBurrow && isBurrowedCache()
  def cloaked   : Boolean = cachedCloaked()
  def detected  : Boolean = isDetectedCache()
  def visible   : Boolean = isVisibleCache()
  
  private val isBurrowedCache = new Cache(() => base.isBurrowed)
  private val cachedCloaked   = new Cache(() => base.isCloaked)
  private val isDetectedCache = new Cache(() => base.isDetected)
  private val isVisibleCache  = new Cache(() => base.isVisible)
  
  //////////////
  // Movement //
  //////////////
  
  def accelerating  : Boolean = base.isAccelerating
  def angleRadians  : Double  = cachedAngleRadians()
  def braking       : Boolean = base.isBraking
  def ensnared      : Boolean = cachedIsEnsnared()
  def flying        : Boolean = cachedFlying()
  def irradiated    : Boolean = cachedIrradiated()
  def lifted        : Boolean = cachedLifted()
  def loaded        : Boolean = cachedLoaded()
  def lockedDown    : Boolean = cachedIsLockedDown()
  def maelstrommed  : Boolean = cachedIsMaelstrommed()
  def sieged        : Boolean = cachedIsSieged()
  def stasised      : Boolean = cachedStasised()
  def stimmed       : Boolean = cachedIsStimmed()
  def stuck         : Boolean = unitClass.canMove && base.isStuck
  def velocityX     : Double  = if (unitClass.canMove || lifted) cacheVelocityX() else 0
  def velocityY     : Double  = if (unitClass.canMove || lifted) cacheVelocityY() else 0
  
  private val cachedAngleRadians    = new Cache(() => base.getAngle)
  private val cacheVelocityX        = new Cache(() => base.getVelocityX)
  private val cacheVelocityY        = new Cache(() => base.getVelocityY)
  private val cachedFlying          = new Cache(() => (unitClass.canFly || lifted)  && base.isFlying)
  private val cachedLifted          = new Cache(() => unitClass.isFlyingBuilding    && base.isLifted)
  private val cachedLoaded          = new Cache(() => unitClass.canBeTransported    && base.isLoaded)
  private val cachedIrradiated      = new Cache(() => unitClass.canBeIrradiated     && base.isIrradiated)
  private val cachedIsSieged        = new Cache(() => unitClass.canSiege            && base.isSieged)
  private val cachedIsStimmed       = new Cache(() => unitClass.canBeStasised       && base.isStimmed)
  private val cachedIsLockedDown    = new Cache(() => unitClass.canBeLockedDown     && base.isLockedDown)
  private val cachedStasised        = new Cache(() => unitClass.canBeStasised       && base.isStasised)
  private val cachedIsEnsnared      = new Cache(() => unitClass.canBeEnsnared       && base.isEnsnared)
  private val cachedIsMaelstrommed  = new Cache(() => unitClass.canBeMaelstrommed   && base.isMaelstrommed)
  
  //////////////
  // Statuses //
  //////////////
  
  def remainingBuildFrames    : Int = remainingBuildFramesCache()
  def remainingUpgradeFrames  : Int = remainingUpgradeFramesCache()
  def remainingTechFrames     : Int = remainingTechFramesCache()
  
  private val remainingBuildFramesCache   = new Cache(() => base.getRemainingBuildTime)
  private val remainingUpgradeFramesCache = new Cache(() => base.getRemainingUpgradeTime)
  private val remainingTechFramesCache    = new Cache(() => base.getRemainingResearchTime)
  
  def beingConstructed    : Boolean = beingConstructedCache()
  def beingGathered       : Boolean = beingGatheredCache()
  def beingHealed         : Boolean = beingHealedCache()
  def blind               : Boolean = blindCache()
  def carryingMinerals    : Boolean = carryingMineralsCache()
  def carryingGas         : Boolean = carryingGasCache()
  def powered             : Boolean = poweredCache()
  def selected            : Boolean = cacheSelected()
  def targetable          : Boolean = targetableCache()
  def underAttack         : Boolean = underAttackCache()
  def underDarkSwarm      : Boolean = underDarkSwarmCache()
  def underDisruptionWeb  : Boolean = underDisruptionWebCache()
  def underStorm          : Boolean = underStormCache()
  
  private val beingConstructedCache = new Cache(() => base.isBeingConstructed)
  private val beingGatheredCache = new Cache(() => base.isBeingGathered)
  private val beingHealedCache = new Cache(() => base.isBeingHealed)
  private val blindCache = new Cache(() => base.isBlind)
  private val poweredCache = new Cache(() => base.isPowered)
  private val targetableCache = new Cache(() => base.isTargetable)
  private val underAttackCache = new Cache(() => base.isUnderAttack)
  private val underDarkSwarmCache = new Cache(() => base.isUnderDarkSwarm)
  private val underDisruptionWebCache = new Cache(() => base.isUnderDisruptionWeb)
  private val underStormCache = new Cache(() => base.isUnderStorm)
  
  private val carryingMineralsCache = new Cache(() => unitClass.isWorker && base.isCarryingMinerals)
  private val carryingGasCache      = new Cache(() => unitClass.isWorker && base.isCarryingGas)
  
  def spiderMines: Int = cachedSpiderMines()
  private val cachedSpiderMines = new Cache(() => base.getSpiderMineCount)
  
  def addon: Option[UnitInfo] = cachedAddon()
  private val cachedAddon = new Cache(() => With.units.get(base.getAddon))
  
  def hasNuke: Boolean = cachedHasNuke()
  private val cachedHasNuke = new Cache(() => base.hasNuke)
  
  def spaceRemaining: Int = spaceRemainingCache()
  private val spaceRemainingCache = new Cache(() => base.getSpaceRemaining)
}
