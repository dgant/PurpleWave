package ProxyBwapi.UnitInfo

import Lifecycle.With
import Mathematics.Points.{Pixel, Tile}
import Performance.Cache
import ProxyBwapi.Players.Players
import ProxyBwapi.Players.PlayerInfo
import ProxyBwapi.Races.{Protoss, Terran}
import ProxyBwapi.UnitClasses.{UnitClass, UnitClasses}
import bwapi.Position

class ForeignUnitInfo(originalBaseUnit: bwapi.Unit, id: Int) extends UnitInfo(originalBaseUnit, id) {

  // Update foreign units
  // Lots of shortcuts are taken here to avoid unnecessary updates
  
  override val foreign: Option[ForeignUnitInfo] = Some(this)
  
  def flagDead()        { _alive              = false }
  def flagMissing()     { _possiblyStillThere = false }
  def flagVisible()     { _visible            = true  }
  def flagInvisible()   { _visible            = false }
  def flagBurrowed()    { _burrowed           = true  }
  def flagCloaked()     { _cloaked            = true  }
  def flagUndetected()  { _detected           = false }
  
  def update(unit: bwapi.Unit) {
    baseUnit = unit
    updateTimeSensitiveInformation()
    updateTimeInsensitiveInformation()
    fixCloakedUnits()
    updateCommon()
  }
  
  private var updateCount = 0
  private def updateTimeInsensitiveInformation() {
    if (updateCount == 0
      || (updateCount + id) % With.configuration.foreignUnitUpdatePeriod == 0) {
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
    _complete           = baseUnit.isCompleted
    _lastSeen           = With.frame
    _possiblyStillThere = true
    _hitPoints          = if (effectivelyCloaked) if (_hitPoints == 0) _unitClass.maxHitPoints  else _hitPoints     else baseUnit.getHitPoints
    _shieldPoints       = if (effectivelyCloaked) if (_hitPoints == 0) _unitClass.maxShields    else _shieldPoints  else baseUnit.getShields
    _pixelCenter        = new Pixel(baseUnit.getPosition)
    _tileTopLeft        = _pixelCenter.subtract(unitClass.dimensionLeft, unitClass.dimensionUp).tileNearest
  }
  
  private def updateTracking() {
    _player     = Players.get(baseUnit.getPlayer)
    _unitClass  = UnitClasses.get(baseUnit.getType)
  }
  
  private var _lastSeen           : Int         = _
  private var _player             : PlayerInfo  = _
  private var _possiblyStillThere : Boolean     = _
  private var _unitClass          : UnitClass   = UnitClasses.None
  
  def lastSeen           : Int        = _lastSeen
  def player             : PlayerInfo = _player
  def possiblyStillThere : Boolean    = _possiblyStillThere
  def unitClass          : UnitClass  = _unitClass
  
  def lastSeenWithin(frames: Int): Boolean = With.framesSince(_lastSeen) < frames
  
  ////////////
  // Health //
  ////////////
  
  private def updateHealth() {
    _defensiveMatrixPoints  = baseUnit.getDefenseMatrixPoints
    _initialResources       = baseUnit.getInitialResources
    _invincible             = baseUnit.isInvincible
    _resourcesLeft          = baseUnit.getResources
    _energy                 = baseUnit.getEnergy
    _plagued                = baseUnit.isPlagued
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
  
  def alive                 : Boolean   = _alive
  def complete              : Boolean   = _complete
  def defensiveMatrixPoints : Int       = _defensiveMatrixPoints
  def hitPoints             : Int       = _hitPoints
  def initialResources      : Int       = _initialResources
  def invincible            : Boolean   = _invincible
  def resourcesLeft         : Int       = _resourcesLeft
  def shieldPoints          : Int       = _shieldPoints
  def energy                : Int       = _energy
  def plagued               : Boolean   = _plagued
  
  ////////////
  // Combat //
  ////////////
  
  def scarabCount       : Int = if (is(Protoss.Reaver)) 3 else 0 // BWAPI probably doens't give this for enemy units. Here's an approximation.
  def interceptorCount  : Int = interceptorCountCache()
  def spiderMines       : Int = spiderMineCountCache()
  private val interceptorCountCache = new Cache(() => if (is(Protoss.Carrier))  baseUnit.getInterceptorCount  else 0)
  private val spiderMineCountCache  = new Cache(() => if (is(Terran.Vulture))   baseUnit.getSpiderMineCount   else 0)
  
  private def updateCombat() {
    _attackStarting           = baseUnit.isStartingAttack
    _attackAnimationHappening = baseUnit.isAttackFrame
    _airWeaponCooldownLeft    = baseUnit.getAirWeaponCooldown
    _groundWeaponCooldownLeft = baseUnit.getGroundWeaponCooldown
    _spellCooldownLeft        = baseUnit.getSpellCooldown
  }
  
  var _interceptors             : Iterable[UnitInfo]  = Iterable.empty
  var _attackStarting           : Boolean             = _
  var _attackAnimationHappening : Boolean             = _
  var _airWeaponCooldownLeft    : Int                 = _
  var _groundWeaponCooldownLeft : Int                 = _
  var _spellCooldownLeft        : Int                 = _
  
  def interceptors              : Iterable[UnitInfo]  = Iterable.empty // BWAPI doesn't publish this for enemy interceptors
  def attackStarting            : Boolean             = ! player.isNeutral && _attackStarting
  def attackAnimationHappening  : Boolean             = ! player.isNeutral && _attackAnimationHappening
  def airCooldownLeft           : Int                 = if (! player.isNeutral) _airWeaponCooldownLeft else 0
  def groundCooldownLeft        : Int                 = if (! player.isNeutral) _groundWeaponCooldownLeft  else 0
  def spellCooldownLeft         : Int                 = if (! player.isNeutral) _spellCooldownLeft  else 0
  
  //////////////
  // Geometry //
  //////////////
  
  private var _pixelCenter : Pixel  = Pixel(0, 0)
  private var _tileTopLeft : Tile   = Tile(0, 0)
  
  def pixelCenter : Pixel   = _pixelCenter
  def tileTopLeft : Tile    = _tileTopLeft
  
  ////////////
  // Orders //
  ////////////
  
  private def updateOrders() {
    //Performance enhancement -- spare the expensive target calls for irrelevant units
    _target               = if (unitClass.targetsMatter)         baseUnit.getTarget               else null
    _targetPosition       = if (unitClass.targetPositionsMatter) baseUnit.getTargetPosition       else null
    _order                = if (unitClass.ordersMatter)          baseUnit.getOrder.toString       else Orders.Nothing
    _orderTarget          = if (unitClass.targetsMatter)         baseUnit.getOrderTarget          else null
    _orderTargetPosition  = if (unitClass.targetPositionsMatter) baseUnit.getOrderTargetPosition  else null
    _gatheringMinerals    = unitClass.ordersMatter && baseUnit.isGatheringMinerals
    _gatheringGas         = unitClass.ordersMatter && baseUnit.isGatheringGas
    _attacking            = unitClass.ordersMatter && baseUnit.isAttacking
    _constructing         = unitClass.ordersMatter && _order == Orders.ConstructingBuilding // baseUnit.isConstructing
    _following            = unitClass.ordersMatter && baseUnit.isFollowing
    _holdingPosition      = unitClass.ordersMatter && baseUnit.isHoldingPosition
    _idle                 = unitClass.ordersMatter && baseUnit.isIdle
    _interruptible        = unitClass.ordersMatter && baseUnit.isInterruptible
    _morphing             = unitClass.ordersMatter && baseUnit.isMorphing
    _repairing            = unitClass.ordersMatter && _order == Orders.Repair // baseUnit.isRepairing doesn't seem to work
    _researching          = unitClass.ordersMatter && baseUnit.isResearching
    _patrolling           = unitClass.ordersMatter && baseUnit.isPatrolling
    _training             = unitClass.ordersMatter && baseUnit.isTraining
    _upgrading            = unitClass.ordersMatter && baseUnit.isUpgrading
  }
  
  private var _target               : bwapi.Unit  = _
  private var _targetPosition       : Position    = Position.None
  private var _order                : String      = "Stop"
  private var _orderTarget          : bwapi.Unit  = _
  private var _orderTargetPosition  : Position    = Position.None
  private var _gatheringMinerals    : Boolean     = _
  private var _gatheringGas         : Boolean     = _
  
  private val badPositions = Vector(Position.Invalid, Position.None, Position.Unknown, null)
  def target            : Option[UnitInfo]    = if (_target == null) None else With.units.get(_target)
  def targetPixel       : Option[Pixel]       = if (badPositions.contains(_targetPosition)) None else Some(new Pixel(_targetPosition))
  def order             : String              = _order
  def orderTarget       : Option[UnitInfo]    = if (_target == null) None else With.units.get(_orderTarget)
  def orderTargetPixel  : Option[Pixel]       = if (badPositions.contains(_orderTargetPosition)) None else Some(new Pixel(_orderTargetPosition))
  def gatheringMinerals : Boolean             = _gatheringMinerals
  def gatheringGas      : Boolean             = _gatheringGas
  
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
  
  def attacking       : Boolean = _attacking
  def constructing    : Boolean = _constructing
  def following       : Boolean = _following
  def holdingPosition : Boolean = _holdingPosition
  def idle            : Boolean = _idle
  def interruptible   : Boolean = _interruptible
  def morphing        : Boolean = _morphing
  def repairing       : Boolean = _repairing
  def teching         : Boolean = _researching
  def patrolling      : Boolean = _patrolling
  def training        : Boolean = _training
  def upgrading       : Boolean = _upgrading
  
  ////////////////
  // Visibility //
  ////////////////
  
  private def updateVisibility() {
    _burrowed = unitClass.canBurrow && baseUnit.isBurrowed
    _cloaked  = ! player.isNeutral && baseUnit.isCloaked
    _detected = player.isNeutral || baseUnit.isDetected
  }
  
  private var _burrowed  : Boolean  = _
  private var _cloaked   : Boolean  = _
  private var _detected  : Boolean  = _
  private var _visible   : Boolean  = _
  
  def burrowed  : Boolean = _burrowed
  def cloaked   : Boolean = _cloaked
  def detected  : Boolean = _detected
  def visible   : Boolean = _visible
  
  //////////////
  // Movement //
  //////////////
  
  private def updateMovement() {
    _accelerating = unitClass.canMove           &&  baseUnit.isAccelerating
    _angle        = if (unitClass.canMove || unitClass.rawCanAttack) baseUnit.getAngle else 0
    _braking      = unitClass.canMove           &&  baseUnit.isBraking
    _ensnared     = ! unitClass.isBuilding      &&  baseUnit.isEnsnared
    _flying       = unitClass.canFly            &&  baseUnit.isFlying
    _irradiated   = unitClass.canBeIrradiated   &&  baseUnit.isIrradiated
    _lifted       = unitClass.isFlyingBuilding  &&  baseUnit.isLifted
    _lockedDown   = unitClass.canBeLockedDown   &&  baseUnit.isLockedDown
    _maelstrommed = unitClass.canBeMaelstrommed &&  baseUnit.isMaelstrommed
    _sieged       = unitClass.canSiege          &&  baseUnit.isSieged
    _stasised     = unitClass.canBeStasised     &&  baseUnit.isStasised
    _stimmed      = unitClass.canStim           &&  baseUnit.isStimmed
    _stuck        = unitClass.canMove           &&  baseUnit.isStuck
    _velocityX    = if(unitClass.canMove)           baseUnit.getVelocityX else 0
    _velocityY    = if(unitClass.canMove)           baseUnit.getVelocityY else 0
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
  
  def accelerating  : Boolean = _accelerating
  def angleRadians  : Double  = _angle
  def braking       : Boolean = _braking
  def ensnared      : Boolean = _ensnared
  def flying        : Boolean = _flying
  def irradiated    : Boolean = _irradiated
  def lifted        : Boolean = _lifted
  def lockedDown    : Boolean = _lockedDown
  def maelstrommed  : Boolean = _maelstrommed
  def sieged        : Boolean = _sieged
  def stasised      : Boolean = _stasised
  def stimmed       : Boolean = _stimmed
  def stuck         : Boolean = _stuck
  def velocityX     : Double  = _velocityX
  def velocityY     : Double  = _velocityY
  
  //////////////
  // Statuses //
  //////////////
  
  private def updateStatuses() {
    _remainingTrainFrames   = if (unitClass.trainsUnits) baseUnit.getRemainingTrainTime else 0
    _remainingUpgradeFrames = if (unitClass.upgradesWhat.nonEmpty) baseUnit.getRemainingUpgradeTime else 0
    _remainingTechFrames    = if (unitClass.techsWhat.nonEmpty) baseUnit.getRemainingResearchTime else 0
    _beingConstructed       = ! player.isNeutral && unitClass.isBuilding && ! complete && (! unitClass.isTerran || baseUnit.isBeingConstructed)
    _beingGathered          = ! player.isNeutral && unitClass.isResource && baseUnit.isBeingGathered
    _beingHealed            = ! player.isNeutral && unitClass.isOrganic && baseUnit.isBeingHealed
    _blind                  = ! player.isNeutral && ! unitClass.isBuilding && baseUnit.isBlind
    _carryingMinerals       = unitClass.isWorker && baseUnit.isCarryingMinerals
    _carryingGas            = unitClass.isWorker && ! carryingMinerals && baseUnit.isCarryingGas
    _powered                = unitClass.requiresPsi && baseUnit.isPowered
    _selected               = false && baseUnit.isSelected
    _targetable             = true || baseUnit.isTargetable // Shortcut for performance; we don't use this anyway
    _underAttack            = ! player.isNeutral && baseUnit.isUnderAttack
    _underDarkSwarm         = ! player.isNeutral && baseUnit.isUnderDarkSwarm
    _underDisruptionWeb     = ! player.isNeutral && baseUnit.isUnderDisruptionWeb
    _underStorm             = ! player.isNeutral && baseUnit.isUnderStorm
    _addon                  = if (unitClass.canBuildAddon) With.units.get(baseUnit.getAddon) else None
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
  
  def remainingCompletionFrames: Int = {
    if (complete) return 0
    val startingHp    = 1 + unitClass.maxHitPoints / 10
    val progress      = Math.max(0.0, (hitPoints - startingHp).toDouble / (unitClass.maxTotalHealth - startingHp))
    val progressLeft  = 1.0 - progress
    val output        = progressLeft * unitClass.buildFrames - With.framesSince(lastSeen)
    output.toInt
  }

  def remainingTrainFrames    : Int     = _remainingTrainFrames
  def remainingUpgradeFrames  : Int     = _remainingUpgradeFrames
  def remainingTechFrames     : Int     = _remainingTechFrames
  def beingConstructed        : Boolean = _beingConstructed
  def beingGathered           : Boolean = _beingGathered
  def beingHealed             : Boolean = _beingHealed
  def blind                   : Boolean = _blind
  def carryingGas             : Boolean = _carryingGas
  def carryingMinerals        : Boolean = _carryingMinerals
  def powered                 : Boolean = _powered
  def selected                : Boolean = _selected
  def targetable              : Boolean = _targetable
  def underAttack             : Boolean = _underAttack
  def underDarkSwarm          : Boolean = _underDarkSwarm
  def underDisruptionWeb      : Boolean = _underDisruptionWeb
  def underStorm              : Boolean = _underStorm
  
  def addon: Option[UnitInfo] = _addon
  def hasNuke: Boolean = false
  
  // Cloaked units show up with 0 hit points/shields.
  // Presumably, if we've never seen them, then they're healthier than that.
  //
  def fixCloakedUnits() {
    if (alive && cloaked && hitPoints == 0) {
      _hitPoints = unitClass.maxHitPoints
      _shieldPoints = unitClass.maxShields
    }
  }
}
