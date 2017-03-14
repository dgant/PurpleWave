package Types.UnitInfo

import Startup.With
import Utilities.Enrichment.EnrichPosition._
import bwapi._

class ForeignUnitInfo(_baseUnit:bwapi.Unit) extends UnitInfo (_baseUnit) {
  
  var _possiblyStillThere = false
  var _alive              = false
  var _lastSeen           = 0
  var _player             = With.game.self
  var _position           = new Position(0,0)
  var _walkPosition       = new WalkPosition(0,0)
  var _tileTopleft        = new TilePosition(0,0)
  var _hitPoints          = 0
  var _shieldPoints       = 0
  var _unitType           = UnitType.None
  var _complete           = false
  var _flying             = false
  var _visible            = false
  var _cloaked            = false
  var _burrowed           = false
  var _detected           = false
  var _morphing           = false
  var _invincible         = false
  var _top                = 0
  var _left               = 0
  var _right              = 0
  var _bottom             = 0
  var _resources          = 0
  var _initialResources   = 0
  
  update(baseUnit)
  
  def update(unit:bwapi.Unit) {
    baseUnit = unit
    _alive              = true
    _possiblyStillThere = true
    _lastSeen           = With.frame
    _player             = unit.getPlayer
    _position           = unit.getPosition
    _walkPosition       = _position.toWalkPosition
    _tileTopleft        = unit.getTilePosition
    _hitPoints          = unit.getHitPoints
    _shieldPoints       = unit.getShields
    _unitType           = unit.getType
    _complete           = unit.isCompleted
    _flying             = unit.isFlying
    _visible            = unit.isVisible
    _cloaked            = unit.isCloaked
    _burrowed           = unit.isBurrowed
    _detected           = unit.isDetected
    _morphing           = unit.isMorphing
    _invincible         = unit.isInvincible
    _top                = unit.getTop
    _left               = unit.getLeft
    _right              = unit.getRight
    _bottom             = unit.getBottom
    _resources          = unit.getResources
    _initialResources   = unit.getInitialResources
  }
  
  def flagDead() {
    _alive = false
  }
  
  def invalidatePosition() {
    _possiblyStillThere = false
  }
  
  override def alive                : Boolean         = _alive
  override def id                   : Int             = _id
  override def lastSeen             : Int             = _lastSeen
  override def possiblyStillThere   : Boolean         = _possiblyStillThere
  override def player               : Player          = _player
  override def position             : Position        = _position
  override def walkPosition         : WalkPosition    = position.toWalkPosition
  override def tileTopLeft          : TilePosition    = _tileTopleft
  override def hitPoints            : Int             = _hitPoints
  override def shieldPoints         : Int             = _shieldPoints
  override def utype                : UnitType        = if (_unitType == UnitType.Terran_Siege_Tank_Tank_Mode && (morphing || ! visible)) UnitType.Terran_Siege_Tank_Siege_Mode else _unitType
  override def complete             : Boolean         = _complete
  override def flying               : Boolean         = _flying
  override def visible              : Boolean         = _visible
  override def cloaked              : Boolean         = _cloaked
  override def burrowed             : Boolean         = _burrowed
  override def detected             : Boolean         = _detected
  override def morphing             : Boolean         = _morphing
  override def invincible           : Boolean         = _invincible
  override def top                  : Int             = _top
  override def left                 : Int             = _left
  override def right                : Int             = _right
  override def bottom               : Int             = _bottom
  override def mineralsLeft         : Int             = if (isMinerals) _resources else 0
  override def gasLeft              : Int             = if (isGas) _resources else 0
  override def initialResources     : Int             = _initialResources
}
