package Types.UnitInfo
import Startup.With
import bwapi._

import Utilities.Enrichment.EnrichPosition._

class FriendlyUnitInfo(var baseUnit:bwapi.Unit) extends UnitInfo {
  override def id: Int = baseUnit.getID
  override def lastSeen: Int = With.game.getFrameCount
  override def possiblyStillThere: Boolean = true
  override def player: Player = baseUnit.getPlayer
  override def position: Position = baseUnit.getPosition
  override def walkPosition: WalkPosition = position.toWalkPosition
  override def tilePosition: TilePosition = baseUnit.getTilePosition
  override def hitPoints: Int = baseUnit.getHitPoints
  override def shieldPoints: Int = baseUnit.getShields
  override def unitType: UnitType = baseUnit.getType
  override def complete: Boolean = baseUnit.isCompleted
}
