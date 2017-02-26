package Types.UnitInfo

import Startup.With
import bwapi._
import Utilities.Enrichment.EnrichPosition._

class EnemyUnitInfo (unit:bwapi.Unit) extends UnitInfo {
  
  var _possiblyStillThere = false
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
  update(unit)
  
  def update(unit:bwapi.Unit) {
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
  }
  
  def invalidatePosition() {
    _possiblyStillThere = false
  }
  
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
}
