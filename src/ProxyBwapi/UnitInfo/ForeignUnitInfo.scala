package ProxyBwapi.UnitInfo

import Lifecycle.With
import Mathematics.Pixels.{Pixel, Tile}
import Performance.Caching.Limiter
import ProxyBwapi.Players.{PlayerInfo, Players}
import ProxyBwapi.Races.{Protoss, Terran}
import ProxyBwapi.UnitClass.{UnitClass, UnitClasses}
import bwapi.Position

class ForeignUnitInfo(baseUnit:bwapi.Unit) extends UnitInfo (baseUnit) {
  
  override def foreign: Option[ForeignUnitInfo] = Some(this)
  
  def flagDead()      { _alive = false }
  def flagMissing()   { _possiblyStillThere = false }
  def flagInvisible() { _visible = false }
  
  def update(unit:bwapi.Unit) {
    base = unit
    updateTimeSensitiveInformation()
    limitMostUpdates.act()
  }
  
  private val limitMostUpdates = new Limiter(1, () => {
    updateTracking()
    if ( ! is(Terran.SpiderMine)) {
      updateHealth()
      updateCombat()
      updateGeometry()
      updateMovement()
      updateOrders()
      updateVisibility()
      updateStatuses()
      fixCloakedUnits()
    }
  })
  
  ///////////////////
  // Tracking info //
  ///////////////////
  
  private def updateTimeSensitiveInformation() {
    _lastSeen           = With.frame
    _possiblyStillThere = true
    _hitPoints          = base.getHitPoints
    _shieldPoints       = base.getShields
    _pixelCenter        = new Pixel(base.getPosition)
    _tileTopLeft        = new Tile(base.getTilePosition)
  }
  
  private def updateTracking() {
    _player     = Players.get(base.getPlayer)
    _unitClass  = UnitClasses.get(base.getType)
  }
  
  private var _lastSeen           : Int         = 0
  private var _player             : PlayerInfo  = null
  private var _possiblyStillThere : Boolean     = false
  private var _unitClass          : UnitClass   = UnitClasses.None
  
  def lastSeen           : Int        = _lastSeen
  def player             : PlayerInfo = _player
  def possiblyStillThere : Boolean    = _possiblyStillThere
  def unitClass          : UnitClass  = _unitClass
  
  def lastSeenWithin(frames: Int) = With.frame - _lastSeen < frames
  
  ////////////
  // Health //
  ////////////
  
  private def updateHealth() {
    
    //_alive is handled via flagDead()
  
    _unitClass              = UnitClasses.get(base.getType)
    
    _complete               = base.isCompleted
    _defensiveMatrixPoints  = base.getDefenseMatrixPoints
    _hitPoints              = if (effectivelyCloaked) if (_hitPoints == 0) _unitClass.maxHitPoints  else _hitPoints     else base.getHitPoints
    _shieldPoints           = if (effectivelyCloaked) if (_hitPoints == 0) _unitClass.maxShields    else _shieldPoints  else base.getShields
    _initialResources       = base.getInitialResources
    _invincible             = base.isInvincible
    _resourcesLeft          = base.getResources
    
    _energy                 = base.getEnergy
    _plagued                = base.isPlagued
    
  }
  
  private var _alive                  : Boolean   = true
  private var _complete               : Boolean   = false
  private var _defensiveMatrixPoints  : Int       = 0
  private var _hitPoints              : Int       = 0
  private var _initialResources       : Int       = 0
  private var _invincible             : Boolean   = false
  private var _resourcesLeft          : Int       = 0
  private var _shieldPoints           : Int       = 0
  private var _energy                 : Int       = 0
  private var _plagued                : Boolean   = false
  
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
  
  val interceptors  : Int = if (is(Protoss.Carrier))  8 else 0
  val scarabs       : Int = if (is(Protoss.Reaver))   5 else 0
  
  private def updateCombat() {
    _attacking                = base.isAttacking
    _attackStarting           = base.isStartingAttack
    _attackAnimationHappening = base.isAttackFrame
    _airWeaponCooldownLeft    = base.getAirWeaponCooldown
    _groundWeaponCooldownLeft = base.getGroundWeaponCooldown
  }
  
  var _attacking                : Boolean = false
  var _attackStarting           : Boolean = false
  var _attackAnimationHappening : Boolean = false
  var _airWeaponCooldownLeft    : Int = 0
  var _groundWeaponCooldownLeft : Int = 0
  
  def attacking                 : Boolean = _attacking
  def attackStarting            : Boolean = _attackStarting
  def attackAnimationHappening  : Boolean = _attackAnimationHappening
  def airCooldownLeft     : Int     = _airWeaponCooldownLeft
  def groundCooldownLeft  : Int     = _groundWeaponCooldownLeft
  
  //////////////
  // Geometry //
  //////////////
  
  private def updateGeometry() {
    _pixelCenter  = new Pixel(base.getPosition)
    _tileTopLeft  = new Tile(base.getTilePosition)
    _top          = base.getTop
    _left         = base.getLeft
    _right        = base.getRight
    _bottom       = base.getBottom
  }
  
  private var _pixelCenter : Pixel  = Pixel(0, 0)
  private var _tileTopLeft : Tile   = new Tile(0, 0)
  private var _top         : Int    = 0
  private var _left        : Int    = 0
  private var _right       : Int    = 0
  private var _bottom      : Int    = 0
  
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
    _target               = base.getTarget
    _targetPosition       = base.getTargetPosition
    _orderTarget          = base.getOrderTarget
    _orderTargetPosition  = base.getOrderTargetPosition
    _gatheringMinerals    = base.isGatheringMinerals
    _gatheringGas         = base.isGatheringGas
  }
  
  private var _target               : bwapi.Unit  = null
  private var _targetPosition       : Position    = Position.None
  private var _orderTarget          : bwapi.Unit  = null
  private var _orderTargetPosition  : Position    = Position.None
  private var _gatheringMinerals    : Boolean     = false
  private var _gatheringGas         : Boolean     = false
  
  private val badPositions = Vector(Position.Invalid, Position.None, Position.Unknown, null)
  def target           : Option[UnitInfo]  = if (_target == null) None else With.units.get(_target)
  def targetPixel      : Option[Pixel]     = if (badPositions.contains(_targetPosition)) None else Some(new Pixel(_targetPosition))
  def orderTarget      : Option[UnitInfo]  = (if (_target == null) None else With.units.get(_orderTarget))
  def orderTargetPixel : Option[Pixel]     = if (badPositions.contains(_orderTargetPosition)) None else Some(new Pixel(_orderTargetPosition))
  
  def gatheringMinerals   : Boolean = base.isGatheringMinerals
  def gatheringGas        : Boolean = base.isGatheringGas
  
  /*
  def attacking:Boolean
  def attackFrame:Boolean
  def constructing:Boolean
  def following:Boolean
  def holdingPixel:Boolean
  def idle:Boolean
  def interruptible:Boolean
  def morphing:Boolean
  def repairing:Boolean
  def researching:Boolean
  def patrolling:Boolean
  def startingAttack:Boolean
  def training:Boolean
  def upgrading:Boolean
  */
  
  ////////////////
  // Visibility //
  ////////////////
  
  private def updateVisibility() {
    _burrowed = base.isBurrowed
    _cloaked  = base.isCloaked
    _detected = base.isDetected
    _visible  = base.isVisible
  }
  
  private var _burrowed  : Boolean  = false
  private var _cloaked   : Boolean  = false
  private var _detected  : Boolean  = false
  private var _visible   : Boolean  = false
  
  def burrowed  : Boolean = _burrowed
  def cloaked   : Boolean = _cloaked
  def detected  : Boolean = _detected
  def visible   : Boolean = _visible
  
  //////////////
  // Movement //
  //////////////
  
  private def updateMovement() {
    _accelerating = base.isAccelerating
    _angle        = base.getAngle
    _braking      = base.isBraking
    _ensnared     = base.isEnsnared
    _flying       = base.isFlying
    _lifted       = base.isLifted
    _lockedDown   = base.isLockedDown
    _maelstrommed = base.isMaelstrommed
    _sieged       = base.isSieged
    _stasised     = base.isStasised
    _stimmed      = base.isStimmed
    _stuck        = base.isStuck
    _velocityX    = base.getVelocityX
    _velocityY    = base.getVelocityY
  }
  
  private var _accelerating   : Boolean  = false
  private var _angle          : Double   = 0.0
  private var _braking        : Boolean  = false
  private var _ensnared       : Boolean  = false
  private var _flying         : Boolean  = false
  private var _lifted         : Boolean  = false
  private var _lockedDown     : Boolean = false
  private var _maelstrommed   : Boolean  = false
  private var _sieged         : Boolean  = false
  private var _stasised       : Boolean  = false
  private var _stimmed        : Boolean  = false
  private var _stuck          : Boolean  = false
  private var _velocityX      : Double   = 0.0
  private var _velocityY      : Double   = 0.0
  
  def accelerating  : Boolean = _accelerating
  def angle         : Double  = _angle
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
    _beingConstructed   = base.isBeingConstructed
    _beingGathered      = base.isBeingGathered
    _beingHealed        = base.isBeingHealed
    _blind              = base.isBlind
    _carryingMinerals   = base.isCarryingMinerals
    _carryingGas        = base.isCarryingGas
    _powered            = base.isPowered
    _selected           = base.isSelected
    _targetable         = base.isTargetable
    _underAttack        = base.isUnderAttack
    _underDarkSwarm     = base.isUnderDarkSwarm
    _underDisruptionWeb = base.isUnderDisruptionWeb
    _underStorm         = base.isUnderStorm
  }
  
  private var _beingConstructed   : Boolean = false
  private var _beingGathered      : Boolean = false
  private var _beingHealed        : Boolean = false
  private var _blind              : Boolean = false
  private var _carryingMinerals   : Boolean = false
  private var _carryingGas        : Boolean = false
  private var _powered            : Boolean = false
  private var _selected           : Boolean = false
  private var _targetable         : Boolean = false
  private var _underAttack        : Boolean = false
  private var _underDarkSwarm     : Boolean = false
  private var _underDisruptionWeb : Boolean = false
  private var _underStorm         : Boolean = false
  
  def beingConstructed   : Boolean = _beingConstructed
  def beingGathered      : Boolean = _beingGathered
  def beingHealed        : Boolean = _beingHealed
  def blind              : Boolean = _blind
  def carryingGas        : Boolean = _carryingGas
  def carryingMinerals   : Boolean = _carryingMinerals
  def powered            : Boolean = _powered
  def selected           : Boolean = _selected
  def targetable         : Boolean = _targetable
  def underAttack        : Boolean = _underAttack
  def underDarkSwarm     : Boolean = _underDarkSwarm
  def underDisruptionWeb : Boolean = _underDisruptionWeb
  def underStorm         : Boolean = _underStorm
  
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
