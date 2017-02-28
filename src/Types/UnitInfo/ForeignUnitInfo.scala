package Types.UnitInfo

import Startup.With
import bwapi._
import Utilities.Enrichment.EnrichPosition._

class ForeignUnitInfo(_baseUnit:bwapi.Unit) extends UnitInfo (_baseUnit) {
  
  var _possiblyStillThere = false
  var _alive              = false
  var _lastSeen           = 0
  var _id                 = 0
  var _player             = With.game.self
  var _position           = new Position(0,0)
  var _walkPosition       = new WalkPosition(0,0)
  var _tilePosition       = new TilePosition(0,0)
  var _hitPoints          = 0
  var _shieldPoints       = 0
  var _unitType           = UnitType.None
  var _complete           = false
  var _flying             = false
  var _cloaked            = false
  var _top                = 0
  var _left               = 0
  var _right              = 0
  var _bottom             = 0
  
  update(baseUnit)
  
  def update(unit:bwapi.Unit) {
    baseUnit = unit
    _alive              = true
    _possiblyStillThere = true
    _lastSeen           = With.game.getFrameCount
    _id                 = unit.getID
    _player             = unit.getPlayer
    _position           = unit.getPosition
    _walkPosition       = _position.toWalkPosition
    _tilePosition       = unit.getTilePosition
    _hitPoints          = unit.getHitPoints
    _shieldPoints       = unit.getShields
    _unitType           = unit.getType
    _complete           = unit.isCompleted
    _flying             = unit.isFlying
    _cloaked            = unit.isCloaked
    _top                = unit.getTop
    _left               = unit.getLeft
    _right              = unit.getRight
    _bottom             = unit.getBottom
  }
  
  def flagDead() {
    _alive = false
  }
  
  def invalidatePosition() {
    _possiblyStillThere = false
  }
  
  override def alive: Boolean = _alive
  override def id: Int = _id
  override def lastSeen: Int = _lastSeen
  override def possiblyStillThere:Boolean = _possiblyStillThere
  override def player: Player = _player
  override def position: Position = _position
  override def walkPosition: WalkPosition = position.toWalkPosition
  override def tilePosition: TilePosition = _tilePosition
  override def hitPoints: Int = _hitPoints
  override def shieldPoints: Int = _shieldPoints
  override def unitType: UnitType = _unitType
  override def complete: Boolean = _complete
  override def flying: Boolean = _flying
  override def visible: Boolean = lastSeen >= With.game.getFrameCount
  override def cloaked: Boolean = _cloaked
  override def top:Int = _top
  override def left:Int = _left
  override def right:Int = _right
  override def bottom:Int = _bottom
}
