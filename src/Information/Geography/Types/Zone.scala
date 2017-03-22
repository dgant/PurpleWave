package Information.Geography.Types

import Startup.With
import bwapi.{Player, TilePosition}
import bwta.Region

import scala.collection.mutable.ListBuffer

class Zone(
  val centroid:TilePosition,
  val region:Region,
  val bases:ListBuffer[Base],
  val edges:ListBuffer[ZoneEdge],
  var owner:Player = With.game.neutral) {
  
}
