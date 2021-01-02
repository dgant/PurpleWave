package ProxyBwapi.UnitInfo

import Lifecycle.With
import Mathematics.Points.{Pixel, Tile}
import Performance.Cache
import ProxyBwapi.Players.{PlayerInfo, Players}
import ProxyBwapi.Races.{Protoss, Terran}
import ProxyBwapi.Techs.Tech
import ProxyBwapi.UnitClasses.{UnitClass, UnitClasses}
import ProxyBwapi.UnitTracking.Visibility
import ProxyBwapi.Upgrades.Upgrade
import bwapi.Position

class ForeignUnitInfo(bwapiUnit: bwapi.Unit, id: Int) extends UnitInfo(bwapiUnit, id) {

  override val foreign: Option[ForeignUnitInfo] = Some(this)

  @inline final def update(): Unit = {
    updateTimeSensitiveInformation()
    updateTimeInsensitiveInformation()
    fixCloakedUnits()
    updateCommon()
    if ( ! lastSeenWithin(24) && is(Terran.SiegeTankUnsieged)) {
      _unitClass = Terran.SiegeTankSieged
    }
  }
  
  private var updateCount = 0
  private def updateTimeInsensitiveInformation() {
    if (updateCount == 0 || (updateCount + id) % With.configuration.foreignUnitUpdatePeriod == 0) {
      updateTracking()
      updateVisibility()
      updateHealth()
      updateCombat()
      updateMovement()
      updateOrders()
      updateStatuses()
    }
    updateCount += 1
  }
  
  ///////////////////
  // Tracking info //
  ///////////////////
  
  private def updateTimeSensitiveInformation() {
    _complete             = bwapiUnit.isCompleted
    _lastSeen             = With.frame
    _pixelCenter          = new Pixel(bwapiUnit.getPosition)
    _pixelCenterObserved  = _pixelCenter
    _tileTopLeft          = new Tile(bwapiUnit.getTilePosition)
    _hitPoints            = if (effectivelyCloaked) if (_hitPoints == 0) _unitClass.maxHitPoints  else _hitPoints     else bwapiUnit.getHitPoints
    _shieldPoints         = if (effectivelyCloaked) if (_hitPoints == 0) _unitClass.maxShields    else _shieldPoints  else bwapiUnit.getShields
  }
  
  private def updateTracking() {
    _player     = Players.get(bwapiUnit.getPlayer)
    _unitClass  = UnitClasses.get(bwapiUnit.getType)
  }

  @inline final def visibility: Visibility.Value = _visibility
  @inline final def setVisbility(value: Visibility.Value): Unit = {
    _visibility = value
    visibility match {
      case Visibility.Visible           => _burrowed = bwapiUnit.isBurrowed
      case Visibility.InvisibleBurrowed => _burrowed = true;  _detected = false
      case Visibility.InvisibleNearby   => _burrowed = false; _detected = false
      case Visibility.InvisibleMissing  => _burrowed = false; _detected = false
      case Visibility.Dead              => _alive = false
    }
  }

  @inline final def presumePixel(value: Pixel): Unit = {
    _pixelCenter = value
  }

  private var _visibility         : Visibility.Value  = Visibility.Visible
  private var _lastSeen           : Int               = _
  private var _player             : PlayerInfo        = _
  private var _unitClass          : UnitClass         = UnitClasses.None
  
  @inline final def lastSeen           : Int        = _lastSeen
  @inline final def player             : PlayerInfo = _player
  @inline final def unitClass          : UnitClass  = _unitClass
  
  @inline final def lastSeenWithin(frames: Int): Boolean = With.framesSince(_lastSeen) < frames
  
  ////////////
  // Health //
  ////////////
  
  private def updateHealth() {
    _defensiveMatrixPoints  = bwapiUnit.getDefenseMatrixPoints
    _initialResources       = bwapiUnit.getInitialResources
    _invincible             = bwapiUnit.isInvincible
    _resourcesLeft          = bwapiUnit.getResources
    _energy                 = bwapiUnit.getEnergy
    _plagued                = bwapiUnit.isPlagued
  }
  
  private var _alive                  : Boolean   = true
  private var _complete               : Boolean   = _
  private var _defensiveMatrixPoints  : Int       = _
  private var _hitPoints              : Int       = _
  private var _initialResources       : Int       = _
  private var _invincible             : Boolean   = _
  private var _resourcesLeft          : Int       = _
  private var _shieldPoints           : Int       = _
  private var _energy                 : Int       = _
  private var _plagued                : Boolean   = _
  
  @inline final def alive                 : Boolean   = _alive
  @inline final def complete              : Boolean   = _complete
  @inline final def defensiveMatrixPoints : Int       = _defensiveMatrixPoints
  @inline final def hitPoints             : Int       = _hitPoints
  @inline final def initialResources      : Int       = _initialResources
  @inline final def invincible            : Boolean   = _invincible
  @inline final def resourcesLeft         : Int       = _resourcesLeft
  @inline final def shieldPoints          : Int       = _shieldPoints
  @inline final def energy                : Int       = _energy
  @inline final def plagued               : Boolean   = _plagued
  
  ////////////
  // Combat //
  ////////////
  
  @inline final def scarabCount       : Int = if (is(Protoss.Reaver)) 3 else 0 // BWAPI probably doens't give this for enemy units. Here's an approximation.
  @inline final def spiderMines       : Int = spiderMineCountCache()
  private val spiderMineCountCache  = new Cache(() => if (is(Terran.Vulture))   bwapiUnit.getSpiderMineCount   else 0)
  
  private def updateCombat() {
    _airWeaponCooldownLeft    = bwapiUnit.getAirWeaponCooldown
    _groundWeaponCooldownLeft = bwapiUnit.getGroundWeaponCooldown
    _spellCooldownLeft        = bwapiUnit.getSpellCooldown
  }
  
  var _interceptors             : Iterable[UnitInfo]  = Iterable.empty
  var _airWeaponCooldownLeft    : Int                 = _
  var _groundWeaponCooldownLeft : Int                 = _
  var _spellCooldownLeft        : Int                 = _
  
  @inline final def interceptors              : Iterable[UnitInfo]  = Iterable.empty // BWAPI doesn't publish this for enemy interceptors
  @inline final def airCooldownLeft           : Int                 = _airWeaponCooldownLeft
  @inline final def groundCooldownLeft        : Int                 = _groundWeaponCooldownLeft
  @inline final def spellCooldownLeft         : Int                 = _spellCooldownLeft
  
  //////////////
  // Geometry //
  //////////////
  
  private var _pixelCenter          : Pixel  = Pixel(0, 0)
  private var _pixelCenterObserved  : Pixel  = Pixel(0, 0)
  private var _tileTopLeft          : Tile   = Tile(0, 0)
  
  @inline final def pixel         : Pixel = _pixelCenter
  @inline final def pixelCenterObserved : Pixel = _pixelCenterObserved
  @inline final def tileTopLeft         : Tile  = _tileTopLeft
  
  ////////////
  // Orders //
  ////////////

  private def convertPosition(position: bwapi.Position): Option[Pixel] = {
    if (badPositions.contains(position)) None else Some(new Pixel(position))
  }

  private def updateOrders() {
    // Performance optmization -- spare the expensive target calls for irrelevant units
    _target               = if (unitClass.targetsMatter)         With.units.get(bwapiUnit.getTarget)               else None
    _targetPixel          = if (unitClass.targetPositionsMatter) convertPosition(bwapiUnit.getTargetPosition)      else None
    _order                = if (unitClass.ordersMatter)          bwapiUnit.getOrder.toString                       else Orders.Nothing
    _orderTarget          = if (unitClass.targetsMatter)         With.units.get(bwapiUnit.getOrderTarget)          else None
    _orderTargetPixel     = if (unitClass.targetPositionsMatter) convertPosition(bwapiUnit.getOrderTargetPosition) else None
    _gatheringMinerals    = unitClass.ordersMatter && bwapiUnit.isGatheringMinerals
    _gatheringGas         = unitClass.ordersMatter && bwapiUnit.isGatheringGas
    _attacking            = unitClass.ordersMatter && bwapiUnit.isAttacking
    _constructing         = unitClass.ordersMatter && _order == Orders.ConstructingBuilding // baseUnit.isConstructing
    _following            = unitClass.ordersMatter && bwapiUnit.isFollowing
    _holdingPosition      = unitClass.ordersMatter && bwapiUnit.isHoldingPosition
    _idle                 = unitClass.ordersMatter && bwapiUnit.isIdle
    _interruptible        = unitClass.ordersMatter && bwapiUnit.isInterruptible
    _morphing             = unitClass.ordersMatter && bwapiUnit.isMorphing
    _repairing            = unitClass.ordersMatter && _order == Orders.Repair // baseUnit.isRepairing doesn't seem to work
    _researching          = unitClass.ordersMatter && bwapiUnit.isResearching
    _patrolling           = unitClass.ordersMatter && bwapiUnit.isPatrolling
    _training             = unitClass.ordersMatter && bwapiUnit.isTraining
    _upgrading            = unitClass.ordersMatter && bwapiUnit.isUpgrading
  }
  
  private var _target               : Option[UnitInfo]  = None
  private var _targetPixel          : Option[Pixel]     = None
  private var _order                : String            = "Stop"
  private var _orderTarget          : Option[UnitInfo]  = None
  private var _orderTargetPixel     : Option[Pixel]     = None
  private var _gatheringMinerals    : Boolean           = _
  private var _gatheringGas         : Boolean           = _
  
  private val badPositions = Vector(Position.Invalid, Position.None, Position.Unknown, null)
  @inline final def target            : Option[UnitInfo]    = _target
  @inline final def targetPixel       : Option[Pixel]       = _targetPixel
  @inline final def order             : String              = _order
  @inline final def orderTarget       : Option[UnitInfo]    = _orderTarget
  @inline final def orderTargetPixel  : Option[Pixel]       = _orderTargetPixel
  @inline final def gatheringMinerals : Boolean             = _gatheringMinerals
  @inline final def gatheringGas      : Boolean             = _gatheringGas
  @inline final def techProducing     : Option[Tech]        = None
  @inline final def upgradeProducing  : Option[Upgrade]     = None
  @inline final def unitProducing     : Option[UnitClass]   = None
  
  private var _attacking        : Boolean = _
  private var _constructing     : Boolean = _
  private var _following        : Boolean = _
  private var _holdingPosition  : Boolean = _
  private var _idle             : Boolean = _
  private var _interruptible    : Boolean = _
  private var _morphing         : Boolean = _
  private var _repairing        : Boolean = _
  private var _researching      : Boolean = _
  private var _patrolling       : Boolean = _
  private var _training         : Boolean = _
  private var _upgrading        : Boolean = _
  
  @inline final def attacking       : Boolean = _attacking
  @inline final def constructing    : Boolean = _constructing
  @inline final def following       : Boolean = _following
  @inline final def holdingPosition : Boolean = _holdingPosition
  @inline final def idle            : Boolean = _idle
  @inline final def interruptible   : Boolean = _interruptible
  @inline final def morphing        : Boolean = _morphing
  @inline final def repairing       : Boolean = _repairing
  @inline final def teching         : Boolean = _researching
  @inline final def patrolling      : Boolean = _patrolling
  @inline final def training        : Boolean = _training
  @inline final def upgrading       : Boolean = _upgrading

  ////////////////
  // Visibility //
  ////////////////
  
  private def updateVisibility() {
    _cloaked  = ! player.isNeutral && bwapiUnit.isCloaked
    _detected = player.isNeutral || bwapiUnit.isDetected
  }

  private var _burrowed  : Boolean  = _
  private var _cloaked   : Boolean  = _
  private var _detected  : Boolean  = _

  @inline final def burrowed  : Boolean = _burrowed
  @inline final def cloaked   : Boolean = _cloaked
  @inline final def detected  : Boolean = _detected
  @inline final def visible   : Boolean = visibility == Visibility.Visible

  //////////////
  // Movement //
  //////////////

  private def updateMovement() {
    _accelerating = unitClass.canMove           &&  bwapiUnit.isAccelerating
    _angle        = if (unitClass.canMove || unitClass.rawCanAttack) bwapiUnit.getAngle else 0
    _braking      = unitClass.canMove           &&  bwapiUnit.isBraking
    _ensnared     = ! unitClass.isBuilding      &&  bwapiUnit.isEnsnared
    _flying       = unitClass.canFly            &&  bwapiUnit.isFlying
    _irradiated   = unitClass.canBeIrradiated   &&  bwapiUnit.isIrradiated
    _lifted       = unitClass.isFlyingBuilding  &&  bwapiUnit.isLifted
    _lockedDown   = unitClass.canBeLockedDown   &&  bwapiUnit.isLockedDown
    _maelstrommed = unitClass.canBeMaelstrommed &&  bwapiUnit.isMaelstrommed
    _sieged       = unitClass.canSiege          &&  bwapiUnit.isSieged
    _stasised     = unitClass.canBeStasised     &&  bwapiUnit.isStasised
    _stimmed      = unitClass.canStim           &&  bwapiUnit.isStimmed
    _stuck        = unitClass.canMove           &&  bwapiUnit.isStuck
    _velocityX    = if(unitClass.canMove)           bwapiUnit.getVelocityX else 0
    _velocityY    = if(unitClass.canMove)           bwapiUnit.getVelocityY else 0
  }

  private var _accelerating   : Boolean   = _
  private var _angle          : Double    = _
  private var _braking        : Boolean   = _
  private var _ensnared       : Boolean   = _
  private var _flying         : Boolean   = _
  private var _irradiated     : Boolean   = _
  private var _lifted         : Boolean   = _
  private var _lockedDown     : Boolean   = _
  private var _maelstrommed   : Boolean   = _
  private var _sieged         : Boolean   = _
  private var _stasised       : Boolean   = _
  private var _stimmed        : Boolean   = _
  private var _stuck          : Boolean   = _
  private var _velocityX      : Double    = _
  private var _velocityY      : Double    = _

  @inline final def accelerating  : Boolean = _accelerating
  @inline final def angleRadians  : Double  = _angle
  @inline final def braking       : Boolean = _braking
  @inline final def ensnared      : Boolean = _ensnared
  @inline final def flying        : Boolean = _flying
  @inline final def irradiated    : Boolean = _irradiated
  @inline final def lifted        : Boolean = _lifted
  @inline final def lockedDown    : Boolean = _lockedDown
  @inline final def maelstrommed  : Boolean = _maelstrommed
  @inline final def sieged        : Boolean = _sieged
  @inline final def stasised      : Boolean = _stasised
  @inline final def stimmed       : Boolean = _stimmed
  @inline final def stuck         : Boolean = _stuck
  @inline final def velocityX     : Double  = _velocityX
  @inline final def velocityY     : Double  = _velocityY

  //////////////
  // Statuses //
  //////////////

  private def updateStatuses() {
    _remainingTrainFrames   = if (unitClass.trainsUnits) bwapiUnit.getRemainingTrainTime else 0
    _remainingUpgradeFrames = if (unitClass.upgradesWhat.nonEmpty) bwapiUnit.getRemainingUpgradeTime else 0
    _remainingTechFrames    = if (unitClass.techsWhat.nonEmpty) bwapiUnit.getRemainingResearchTime else 0
    _beingConstructed       = ! player.isNeutral && unitClass.isBuilding && ! complete && (! unitClass.isTerran || bwapiUnit.isBeingConstructed)
    _beingGathered          = ! player.isNeutral && unitClass.isResource && bwapiUnit.isBeingGathered
    _beingHealed            = ! player.isNeutral && unitClass.isOrganic && bwapiUnit.isBeingHealed
    _blind                  = ! player.isNeutral && ! unitClass.isBuilding && bwapiUnit.isBlind
    _carryingMinerals       = unitClass.isWorker && bwapiUnit.isCarryingMinerals
    _carryingGas            = unitClass.isWorker && ! carryingMinerals && bwapiUnit.isCarryingGas
    _powered                = unitClass.requiresPsi && bwapiUnit.isPowered
    _selected               = bwapiUnit.isSelected
    _targetable             = bwapiUnit.isTargetable
    _underAttack            = ! player.isNeutral && bwapiUnit.isUnderAttack
    _underDarkSwarm         = ! player.isNeutral && bwapiUnit.isUnderDarkSwarm
    _underDisruptionWeb     = ! player.isNeutral && bwapiUnit.isUnderDisruptionWeb
    _underStorm             = ! player.isNeutral && bwapiUnit.isUnderStorm
    _addon                  = if (unitClass.canBuildAddon) With.units.get(bwapiUnit.getAddon) else None
    _removalTimer           = bwapiUnit.getRemoveTimer
  }

  private var _remainingTrainFrames   : Int = _
  private var _remainingUpgradeFrames : Int = _
  private var _remainingTechFrames    : Int = _
  private var _beingConstructed       : Boolean = _
  private var _beingGathered          : Boolean = _
  private var _beingHealed            : Boolean = _
  private var _blind                  : Boolean = _
  private var _carryingMinerals       : Boolean = _
  private var _carryingGas            : Boolean = _
  private var _powered                : Boolean = _
  private var _selected               : Boolean = _
  private var _targetable             : Boolean = _
  private var _underAttack            : Boolean = _
  private var _underDarkSwarm         : Boolean = _
  private var _underDisruptionWeb     : Boolean = _
  private var _underStorm             : Boolean = _
  private var _addon                  : Option[UnitInfo] = None
  private var _removalTimer           : Int = _

  protected def remainingFrames(snapshotHitPoints: Int, snapshotShields: Int, dataFrame: Int): Int = {
    val totalHealthInitial  = 1 + unitClass.maxTotalHealth / 10
    val totalHealthSnapshot = snapshotHitPoints + snapshotShields
    val progress            = Math.max(0.0, (totalHealthSnapshot - totalHealthInitial).toDouble / (unitClass.maxTotalHealth - totalHealthInitial))
    val progressLeft        = 1.0 - progress
    val output              = progressLeft * unitClass.buildFrames - With.framesSince(dataFrame)
    output.toInt
  }
  @inline final def remainingCompletionFrames: Int = {
    if (complete) return 0
    // Use both the initial projection and the up-to-date projection
    // We can't always trust the most up-to-date projection in case the unit has taken damage
    val remainingNow      = remainingFrames(hitPoints, shieldPoints, lastSeen)
    val remainingInitial  = remainingFrames(initialHitPoints, initialShields, frameDiscovered)
    val output            = Math.min(remainingNow, remainingInitial)
    output
  }

  @inline final def remainingTrainFrames    : Int     = _remainingTrainFrames
  @inline final def remainingUpgradeFrames  : Int     = _remainingUpgradeFrames
  @inline final def remainingTechFrames     : Int     = _remainingTechFrames
  @inline final def beingConstructed        : Boolean = _beingConstructed
  @inline final def beingGathered           : Boolean = _beingGathered
  @inline final def beingHealed             : Boolean = _beingHealed
  @inline final def blind                   : Boolean = _blind
  @inline final def carryingGas             : Boolean = _carryingGas
  @inline final def carryingMinerals        : Boolean = _carryingMinerals
  @inline final def powered                 : Boolean = _powered
  @inline final def selected                : Boolean = _selected
  @inline final def targetable              : Boolean = _targetable
  @inline final def underAttack             : Boolean = _underAttack
  @inline final def underDarkSwarm          : Boolean = _underDarkSwarm
  @inline final def underDisruptionWeb      : Boolean = _underDisruptionWeb
  @inline final def underStorm              : Boolean = _underStorm
  
  @inline final def addon: Option[UnitInfo] = _addon
  @inline final def hasNuke: Boolean = false
  @inline final def framesUntilRemoval: Int = _removalTimer
  
  // Cloaked units show up with 0 hit points/shields.
  // Presumably, if we've never seen them, then they're healthier than that.
  @inline final def fixCloakedUnits() {
    if (alive && cloaked && hitPoints == 0) {
      _hitPoints = unitClass.maxHitPoints
      _shieldPoints = unitClass.maxShields
    }
  }
}
