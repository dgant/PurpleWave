package ProxyBwapi.UnitInfo

import Lifecycle.With
import Mathematics.Points.{Pixel, Tile}
import Performance.Caching.{CacheFrame, Limiter}
import ProxyBwapi.Players.{PlayerInfo, Players}
import ProxyBwapi.Races.Protoss
import ProxyBwapi.UnitClass.{UnitClass, UnitClasses}
import bwapi.{Position, UnitCommand}

class ForeignUnitInfo(originalBaseUnit: bwapi.Unit) extends UnitInfo (originalBaseUnit) {
  
  override def foreign: Option[ForeignUnitInfo] = Some(this)
  
  def flagDead()        { _alive              = false }
  def flagMissing()     { _possiblyStillThere = false }
  def flagInvisible()   { _visible            = false }
  def flagBurrowed()    { _burrowed           = true  }
  def flagCloaked()     { _cloaked            = true  }
  def flagUndetected()  { _detected           = false }
  
  def update(unit: bwapi.Unit) {
    baseUnit = unit
    updateTimeSensitiveInformation()
    limitMostUpdates.act()
    fixCloakedUnits()
    updateHistory()
  }
  
  private val limitMostUpdates = new Limiter(1, () => {
    updateTracking()
    updateVisibility()
    updateHealth()
    updateCombat()
    updateGeometry()
    updateMovement()
    updateOrders()
    updateStatuses()
  })
  
  ///////////////////
  // Tracking info //
  ///////////////////
  
  private def updateTimeSensitiveInformation() {
    _lastSeen           = With.frame
    _possiblyStillThere = true
    _hitPoints          = if (effectivelyCloaked) if (_hitPoints == 0) _unitClass.maxHitPoints  else _hitPoints     else baseUnit.getHitPoints
    _shieldPoints       = if (effectivelyCloaked) if (_hitPoints == 0) _unitClass.maxShields    else _shieldPoints  else baseUnit.getShields
    _pixelCenter        = new Pixel(baseUnit.getPosition)
    _tileTopLeft        = new Tile(baseUnit.getTilePosition)
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
    
    //_alive is handled via flagDead()
  
    _unitClass              = UnitClasses.get(baseUnit.getType)
    
    _complete               = baseUnit.isCompleted
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
  def interceptorCount  : Int = interceptorCountCache.get
  private val interceptorCountCache = new CacheFrame(() => baseUnit.getInterceptorCount)
  
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
  def attackStarting            : Boolean             = _attackStarting
  def attackAnimationHappening  : Boolean             = _attackAnimationHappening
  def airCooldownLeft           : Int                 = _airWeaponCooldownLeft
  def groundCooldownLeft        : Int                 = _groundWeaponCooldownLeft
  def spellCooldownLeft         : Int                 = _spellCooldownLeft
  
  //////////////
  // Geometry //
  //////////////
  
  private def updateGeometry() {
    _pixelCenter  = new Pixel(baseUnit.getPosition)
    _tileTopLeft  = new Tile(baseUnit.getTilePosition)
    _top          = baseUnit.getTop
    _left         = baseUnit.getLeft
    _right        = baseUnit.getRight
    _bottom       = baseUnit.getBottom
  }
  
  private var _pixelCenter : Pixel  = Pixel(0, 0)
  private var _tileTopLeft : Tile   = Tile(0, 0)
  private var _top         : Int    = _
  private var _left        : Int    = _
  private var _right       : Int    = _
  private var _bottom      : Int    = _
  
  def pixelCenter : Pixel   = _pixelCenter
  def tileTopLeft : Tile    = _tileTopLeft
  def top         : Int     = _top
  def left        : Int     = _left
  def right       : Int     = _right
  def bottom      : Int     = _bottom
  
  ////////////
  // Orders //
  ////////////
  
  private def updateOrders() {
    //Performance enhancement -- spare the expensive target calls for irrelevant units
    _target               = if (unitClass.targetsMatter)         baseUnit.getTarget         else null
    _targetPosition       = if (unitClass.targetPositionsMatter) baseUnit.getTargetPosition else null
    _command              = baseUnit.getLastCommand
    _order                = baseUnit.getOrder.toString
    _orderTarget          = baseUnit.getOrderTarget
    _orderTargetPosition  = baseUnit.getOrderTargetPosition
    _gatheringMinerals    = baseUnit.isGatheringMinerals
    _gatheringGas         = baseUnit.isGatheringGas
    _attacking            = baseUnit.isAttacking
    _constructing         = baseUnit.isConstructing
    _following            = baseUnit.isFollowing
    _holdingPosition      = baseUnit.isHoldingPosition
    _idle                 = baseUnit.isIdle
    _interruptible        = baseUnit.isInterruptible
    _morphing             = baseUnit.isMorphing
    _repairing            = baseUnit.isRepairing
    _researching          = baseUnit.isResearching
    _patrolling           = baseUnit.isPatrolling
    _training             = baseUnit.isTraining
    _upgrading            = baseUnit.isUpgrading
  }
  
  private var _target               : bwapi.Unit  = _
  private var _targetPosition       : Position    = Position.None
  private var _command              : UnitCommand = _
  private var _order                : String      = "Stop"
  private var _orderTarget          : bwapi.Unit  = _
  private var _orderTargetPosition  : Position    = Position.None
  private var _gatheringMinerals    : Boolean     = _
  private var _gatheringGas         : Boolean     = _
  
  private val badPositions = Vector(Position.Invalid, Position.None, Position.Unknown, null)
  def target            : Option[UnitInfo]    = if (_target == null) None else With.units.get(_target)
  def targetPixel       : Option[Pixel]       = if (badPositions.contains(_targetPosition)) None else Some(new Pixel(_targetPosition))
  def command           : Option[UnitCommand] = Option(_command)
  def order             : String              = _order
  def orderTarget       : Option[UnitInfo]    = if (_target == null) None else With.units.get(_orderTarget)
  def orderTargetPixel  : Option[Pixel]       = if (badPositions.contains(_orderTargetPosition)) None else Some(new Pixel(_orderTargetPosition))
  
  def gatheringMinerals   : Boolean = baseUnit.isGatheringMinerals
  def gatheringGas        : Boolean = baseUnit.isGatheringGas
  
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
    _burrowed = baseUnit.isBurrowed
    _cloaked  = baseUnit.isCloaked
    _detected = baseUnit.isDetected
    _visible  = baseUnit.isVisible
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
    _accelerating = baseUnit.isAccelerating
    _angle        = baseUnit.getAngle
    _braking      = baseUnit.isBraking
    _ensnared     = baseUnit.isEnsnared
    _flying       = baseUnit.isFlying
    _lifted       = baseUnit.isLifted
    _lockedDown   = baseUnit.isLockedDown
    _maelstrommed = baseUnit.isMaelstrommed
    _sieged       = baseUnit.isSieged
    _stasised     = baseUnit.isStasised
    _stimmed      = baseUnit.isStimmed
    _stuck        = baseUnit.isStuck
    _velocityX    = baseUnit.getVelocityX
    _velocityY    = baseUnit.getVelocityY
  }
  
  private var _accelerating   : Boolean   = _
  private var _angle          : Double    = _
  private var _braking        : Boolean   = _
  private var _ensnared       : Boolean   = _
  private var _flying         : Boolean   = _
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
    _remainingUpgradeFrames = baseUnit.getRemainingUpgradeTime
    _remainingTechFrames    = baseUnit.getRemainingResearchTime
    _beingConstructed       = baseUnit.isBeingConstructed
    _beingGathered          = baseUnit.isBeingGathered
    _beingHealed            = baseUnit.isBeingHealed
    _blind                  = baseUnit.isBlind
    _carryingMinerals       = baseUnit.isCarryingMinerals
    _carryingGas            = baseUnit.isCarryingGas
    _powered                = baseUnit.isPowered
    _selected               = baseUnit.isSelected
    _targetable             = baseUnit.isTargetable
    _underAttack            = baseUnit.isUnderAttack
    _underDarkSwarm         = baseUnit.isUnderDarkSwarm
    _underDisruptionWeb     = baseUnit.isUnderDisruptionWeb
    _underStorm             = baseUnit.isUnderStorm
    _addon                  = With.units.get(baseUnit.getAddon)
  }
  
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
  
  def remainingBuildFrames: Int = {
    val startingHp    = 1 + unitClass.maxHitPoints / 10
    val progress      = (hitPoints - startingHp) / (unitClass.maxTotalHealth - startingHp).toDouble
    val progressLeft  = 1.0 - progress
    val output        = progress * unitClass.buildFrames
    output.toInt
  }
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
