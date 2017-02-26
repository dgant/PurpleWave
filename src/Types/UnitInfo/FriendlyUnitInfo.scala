package Types.UnitInfo
import Startup.With
import bwapi._

import Utilities.Enrichment.EnrichPosition._

class FriendlyUnitInfo(var _unit:bwapi.Unit) extends UnitInfo {
  override def id: Int = _unit.getID
  override def lastSeen: Int = With.game.getFrameCount
  override def possiblyStillThere: Boolean = true
  override def player: Player = _unit.getPlayer
  override def position: Position = _unit.getPosition
  override def walkPosition: WalkPosition = position.toWalkPosition
  override def tilePosition: TilePosition = _unit.getTilePosition
  override def hitPoints: Int = _unit.getHitPoints
  override def shieldPoints: Int = _unit.getShields
  override def unitType: UnitType = _unit.getType
  override def complete: Boolean = _unit.isCompleted
}
