package ProxyBwapi.UnitInfo
import Lifecycle.With
import Mathematics.Points.{Pixel, Tile}
import Performance.Cache
import ProxyBwapi.Players.{PlayerInfo, Players}
import ProxyBwapi.Races.{Protoss, Terran, Zerg}
import ProxyBwapi.Techs.{Tech, Techs}
import ProxyBwapi.UnitClasses.{UnitClass, UnitClasses}
import ProxyBwapi.Upgrades.{Upgrade, Upgrades}
import bwapi._

import scala.collection.JavaConverters._

abstract class FriendlyUnitProxy(base: bwapi.Unit, id: Int) extends UnitInfo(base, id) {

  override protected val cd1: Int = With.configuration.friendlyUnitUpdatePeriod
  override protected val cd2: Int = cd1 * 2
  override protected val cd4: Int = cd1 * 4
  
  override def equals(obj: Any): Boolean = obj.isInstanceOf[FriendlyUnitProxy] && obj.asInstanceOf[FriendlyUnitProxy].id == id
  override def hashCode(): Int = id.hashCode
  
  private val cacheClass     = new Cache[UnitClass]  (() =>  UnitClasses.get(base.getType))
  private val cachePlayer    = new Cache[PlayerInfo] (() =>  Players.get(base.getPlayer), 24)
  private val cachePixel     = new Cache[Pixel]      (() =>  new Pixel(base.getPosition))
  private val cacheTile      = new Cache[Tile]       (() =>  new Tile(base.getTilePosition))
  private val cacheCompleted = new Cache[Boolean]    (() =>  base.isCompleted)
  private val cacheExists    = new Cache[Boolean]    (() =>  { val e = base.exists; With.performance.trackUnit(id, e); e })
  private val cacheSelected  = new Cache[Boolean]    (() =>  base.isSelected, cd1)
  
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

  private val getHitPointsCache             = new Cache(() => base.getHitPoints, cd1)
  private val isInvincibleCache             = new Cache(() => base.isInvincible, cd4)
  private val isPlaguedCache                = new Cache(() => base.isPlagued)
  private val getDefenseMatrixPointsCache   = new Cache(() => if (unitClass.isBuilding) 0 else base.getDefenseMatrixPoints)
  private val getInitialResourcesCache      = new Cache(() => if (unitClass.isResource) base.getInitialResources else 0)
  private val getResourcesCache             = new Cache(() => if (unitClass.isResource) base.getResources else 0)
  private val getShieldsCache               = new Cache(() => if (unitClass.isProtoss) base.getShields else 0, cd1)
  private val getEnergyCache                = new Cache(() => if (unitClass.maxEnergy > 0) base.getEnergy else 0)
  
  ////////////
  // Combat //
  ////////////
  
  def interceptors      : Iterable[UnitInfo] = interceptorsCache()
  def interceptorCount  : Int = interceptorCountCache()
  def scarabCount       : Int = scarabCountCache()
  
  private val interceptorsCache       = new Cache(() => if (is(Protoss.Carrier)) base.getInterceptors.asScala.flatMap(With.units.get) else List.empty)
  private val interceptorCountCache   = new Cache(() => if (is(Protoss.Carrier)) base.getInterceptorCount else 0)
  private val scarabCountCache        = new Cache(() => if (is(Protoss.Reaver)) base.getScarabCount else 0)
  
  def attackStarting            : Boolean = base.isStartingAttack
  def attackAnimationHappening  : Boolean = base.isAttackFrame
  def airCooldownLeft           : Int     = base.getAirWeaponCooldown
  def groundCooldownLeft        : Int     = base.getGroundWeaponCooldown
  def spellCooldownLeft         : Int     = spellCooldownLeftCache()
  
  private val isStartingAttackCache   = new Cache(() => unitClass.rawCanAttack && base.isStartingAttack)
  private val isAttackFrameCache      = new Cache(() => unitClass.rawCanAttack && base.isAttackFrame)
  private val airCooldownLeftCache    = new Cache(() => base.getAirWeaponCooldown)
  private val groundCooldownLeftCache = new Cache(() => base.getGroundWeaponCooldown)
  private val spellCooldownLeftCache  = new Cache(() => base.getSpellCooldown)
  
  //////////////
  // Geometry //
  //////////////
  
  def pixelCenter : Pixel = cachePixel()
  def tileTopLeft : Tile  = cacheTile()
  
  ////////////
  // Orders //
  ////////////
  
  def gatheringMinerals : Boolean = isGatheringMineralsCache()
  def gatheringGas      : Boolean = isGatheringGasCache()
  
  private val isGatheringMineralsCache  = new Cache(() => base.isGatheringMinerals)
  private val isGatheringGasCache       = new Cache(() => base.isGatheringGas)
  
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
  private val isConstructingCache     = new Cache(() => unitClass.isWorker && base.isConstructing)
  private val isFollowingCache        = new Cache(() => base.isFollowing)
  private val isHoldingPositionCache  = new Cache(() => base.isHoldingPosition)
  private val isIdleCache             = new Cache(() => base.isIdle)
  private val isInterruptibleCache    = new Cache(() => base.isInterruptible)
  private val isMorphingCache         = new Cache(() => base.isMorphing)
  private val isRepairingCache        = new Cache(() => unitClass.isWorker && unitClass.isTerran && base.isRepairing)
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
  
  def burrowed  : Boolean = unitClass.canBurrow && isBurrowedCache()
  def cloaked   : Boolean = cachedCloaked()
  def detected  : Boolean = isDetectedCache()
  def visible   : Boolean = isVisibleCache()
  
  private val isBurrowedCache = new Cache(() => base.isBurrowed)
  private val cachedCloaked   = new Cache(() => base.isCloaked)
  private val isDetectedCache = new Cache(() => base.isDetected)
  private val isVisibleCache  = new Cache(() => true || base.isVisible) // Performance hack -- when wouldn't it be visible?
  
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
  private val cachedFlying          = new Cache(() => (unitClass.canFly || lifted)  && base.isFlying, cd4)
  private val cachedLifted          = new Cache(() => unitClass.isFlyingBuilding    && base.isLifted, cd4)
  private val cachedLoaded          = new Cache(() => unitClass.canBeTransported    && base.isLoaded)
  private val cachedIrradiated      = new Cache(() => unitClass.canBeIrradiated     && Players.all.exists(_.hasTech(Terran.Irradiate)) && base.isIrradiated)
  private val cachedIsSieged        = new Cache(() => unitClass.canSiege            && base.isSieged)
  private val cachedIsStimmed       = new Cache(() => unitClass.canStim             && base.isStimmed)
  private val cachedIsLockedDown    = new Cache(() => unitClass.canBeLockedDown     && Players.all.exists(_.hasTech(Terran.Lockdown))   && base.isLockedDown,   cd4)
  private val cachedStasised        = new Cache(() => unitClass.canBeStasised       && Players.all.exists(_.hasTech(Protoss.Stasis))    && base.isStasised,     cd4)
  private val cachedIsEnsnared      = new Cache(() => unitClass.canBeEnsnared       && Players.all.exists(_.hasTech(Zerg.Ensnare))      && base.isEnsnared,     cd4)
  private val cachedIsMaelstrommed  = new Cache(() => unitClass.canBeMaelstrommed   && Players.all.exists(_.hasTech(Protoss.Maelstrom)) && base.isMaelstrommed, cd4)
  
  //////////////
  // Statuses //
  //////////////
  
  def remainingCompletionFrames : Int = remainingBuildFramesCache()
  def remainingUpgradeFrames    : Int = remainingUpgradeFramesCache()
  def remainingTechFrames       : Int = remainingTechFramesCache()
  def remainingTrainFrames      : Int = remainingTrainFramesCache()
  
  private val remainingTrainFramesCache   = new Cache(() => if ( ! unitClass.trainsUnits) 0 else base.getRemainingTrainTime)
  private val remainingBuildFramesCache   = new Cache(() => if (complete) 0 else base.getRemainingBuildTime)
  private val remainingUpgradeFramesCache = new Cache(() => if (unitClass.upgradesWhat.isEmpty) 0 else base.getRemainingUpgradeTime)
  private val remainingTechFramesCache    = new Cache(() => if (unitClass.techsWhat.isEmpty) 0 else base.getRemainingResearchTime)
  
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
  private val poweredCache = new Cache(() => base.isPowered, cd4)
  private val targetableCache = new Cache(() => base.isTargetable)
  private val underAttackCache = new Cache(() => base.isUnderAttack)
  private val underDarkSwarmCache = new Cache(() => base.isUnderDarkSwarm, cd4)
  private val underDisruptionWebCache = new Cache(() => base.isUnderDisruptionWeb, cd4)
  private val underStormCache = new Cache(() => base.isUnderStorm)
  
  private val carryingMineralsCache = new Cache(() => unitClass.isWorker && base.isCarryingMinerals)
  private val carryingGasCache      = new Cache(() => unitClass.isWorker && ! carryingMinerals && base.isCarryingGas)
  
  def spiderMines: Int = cachedSpiderMines()
  private val cachedSpiderMines = new Cache(() => base.getSpiderMineCount, cd2)
  
  def addon: Option[UnitInfo] = cachedAddon()
  private val cachedAddon = new Cache(() =>
    if (unitClass.isTerran
    && unitClass.isBuilding
    && (unitClass.isTownHall || unitClass.isFactory || unitClass.isStarport || unitClass.isScienceFacility))
    With.units.get(base.getAddon) else None)
  
  def hasNuke: Boolean = cachedHasNuke()
  private val cachedHasNuke = new Cache(() => base.hasNuke, cd4)
  
  def spaceRemaining: Int = spaceRemainingCache()
  private val spaceRemainingCache = new Cache(() => base.getSpaceRemaining)
  
  def kills: Int = killsCache()
  private val killsCache = new Cache(() => base.getKillCount)
}
