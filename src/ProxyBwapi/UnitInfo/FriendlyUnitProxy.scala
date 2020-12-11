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

abstract class FriendlyUnitProxy(bwapiUnit: bwapi.Unit, id: Int) extends UnitInfo(bwapiUnit, id) {

  override def equals(obj: Any): Boolean = obj.isInstanceOf[FriendlyUnitProxy] && obj.asInstanceOf[FriendlyUnitProxy].id == id
  override def hashCode(): Int = id.hashCode

  private val cacheClass     = new Cache[UnitClass]  (() =>  UnitClasses.get(bwapiUnit.getType))
  private val cachePlayer    = new Cache[PlayerInfo] (() =>  Players.get(bwapiUnit.getPlayer))
  private val cachePixel     = new Cache[Pixel]      (() =>  new Pixel(bwapiUnit.getPosition))
  private val cacheTile      = new Cache[Tile]       (() =>  new Tile(bwapiUnit.getTilePosition))
  private val cacheExists    = new Cache[Boolean]    (() =>  bwapiUnit.exists)

  ///////////////////
  // Tracking info //
  ///////////////////

  def player              : PlayerInfo  = cachePlayer()
  def unitClass           : UnitClass   = cacheClass()
  def lastSeen            : Int         = With.frame

  def update(newBase: bwapi.Unit) {
    baseUnit = newBase
    updateCommon()
  }

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

  def interceptors      : Iterable[UnitInfo] = interceptorsCache()
  def interceptorCount  : Int = bwapiUnit.getInterceptorCount
  def scarabCount       : Int = bwapiUnit.getScarabCount

  private val interceptorsCache = new Cache(() => bwapiUnit.getInterceptors.asScala.flatMap(With.units.get))

  def attackStarting            : Boolean = bwapiUnit.isStartingAttack
  def attackAnimationHappening  : Boolean = bwapiUnit.isAttackFrame
  def airCooldownLeft           : Int     = bwapiUnit.getAirWeaponCooldown
  def groundCooldownLeft        : Int     = bwapiUnit.getGroundWeaponCooldown
  def spellCooldownLeft         : Int     = bwapiUnit.getSpellCooldown

  //////////////
  // Geometry //
  //////////////

  def pixelCenter : Pixel = cachePixel()
  def tileTopLeft : Tile  = cacheTile()

  ////////////
  // Orders //
  ////////////

  def gatheringMinerals : Boolean = bwapiUnit.isGatheringMinerals
  def gatheringGas      : Boolean = bwapiUnit.isGatheringGas

  private val badPositions = Vector(null, Position.Invalid, Position.None, Position.Unknown)

  def target            : Option[UnitInfo]  = targetCache()
  def targetPixel       : Option[Pixel]     = targetPixelCache()
  def order             : String            = bwapiUnit.getOrder.toString
  def orderTarget       : Option[UnitInfo]  = orderTargetCache()
  def orderTargetPixel  : Option[Pixel]     = orderTargetPixelCache()

  private val orderTargetCache = new Cache(() => {
    val target = if (unitClass.targetsMatter) bwapiUnit.getOrderTarget else null
    if (target == null) None else With.units.get(target)
  })
  private val orderTargetPixelCache = new Cache(() => {
    val position = if (unitClass.targetPositionsMatter) bwapiUnit.getOrderTargetPosition else null
    if (badPositions.contains(position)) None else Some(new Pixel(position))
  })
  private val targetCache = new Cache(() => {
    val target = if (unitClass.targetsMatter) bwapiUnit.getTarget else null
    if (target == null) None else With.units.get(target)
  })
  private val targetPixelCache = new Cache(() => {
    val position = if (unitClass.targetPositionsMatter) bwapiUnit.getTargetPosition else null
    if (badPositions.contains(position)) None else Some(new Pixel(position))
  })

  def attacking       : Boolean = bwapiUnit.isAttacking
  def constructing    : Boolean = bwapiUnit.isConstructing
  def following       : Boolean = bwapiUnit.isFollowing
  def holdingPosition : Boolean = bwapiUnit.isHoldingPosition
  def idle            : Boolean = bwapiUnit.isIdle
  def interruptible   : Boolean = bwapiUnit.isInterruptible
  def morphing        : Boolean = bwapiUnit.isMorphing
  def repairing       : Boolean = bwapiUnit.isRepairing
  def teching         : Boolean = bwapiUnit.isResearching
  def upgrading       : Boolean = bwapiUnit.isUpgrading
  def patrolling      : Boolean = bwapiUnit.isPatrolling
  def training        : Boolean = bwapiUnit.isTraining

  def command: Option[UnitCommand]  = getLastCommandCache()
  def commandFrame: Int             = bwapiUnit.getLastCommandFrame

  private val getLastCommandCache = new Cache(() => Option(bwapiUnit.getLastCommand) )

  def buildType: UnitClass = buildTypeCache()
  def trainingQueue: Iterable[UnitClass] = trainingQueueCache()
  def techProducing: Option[Tech] = techProducingCache()
  def upgradeProducing: Option[Upgrade] = upgradeProducingCache()
  def unitProducing: Option[UnitClass] = trainingQueue.headOption

  private val buildTypeCache = new Cache(() => UnitClasses.get(bwapiUnit.getBuildType))
  private val trainingQueueCache = new Cache(() =>
    if (unitClass.trainsUnits)
      bwapiUnit.getTrainingQueue.asScala.map(UnitClasses.get)
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

  def remainingCompletionFrames : Int = if (complete) 0 else bwapiUnit.getRemainingBuildTime
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
  def targetable          : Boolean = bwapiUnit.isTargetable
  def underAttack         : Boolean = bwapiUnit.isUnderAttack
  def underDarkSwarm      : Boolean = bwapiUnit.isUnderDarkSwarm
  def underDisruptionWeb  : Boolean = bwapiUnit.isUnderDisruptionWeb
  def underStorm          : Boolean = bwapiUnit.isUnderStorm

  def spiderMines: Int = bwapiUnit.getSpiderMineCount

  def addon: Option[UnitInfo] = cachedAddon()
  private val cachedAddon = new Cache(() =>
    if (unitClass.isTerran
    && unitClass.isBuilding
    && (unitClass.isTownHall || unitClass.isFactory || unitClass.isStarport || unitClass.isScienceFacility))
    With.units.get(bwapiUnit.getAddon) else None)

  def hasNuke: Boolean = bwapiUnit.hasNuke

  def framesUntilRemoval: Int = bwapiUnit.getRemoveTimer

  def spaceRemaining: Int = bwapiUnit.getSpaceRemaining

  def kills: Int = bwapiUnit.getKillCount
}
