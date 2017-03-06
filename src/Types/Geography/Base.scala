package Types.Geography

import Geometry.TileRectangle
import Startup.With
import bwapi.{Player, TilePosition}
import bwta.{Chokepoint, Region}

import scala.collection.mutable.ListBuffer

class Zone(
  val centroid:TilePosition,
  val region:Region,
  val bases:ListBuffer[Base],
  val edges:ListBuffer[ZoneEdge],
  var owner:Player = With.game.neutral)

class ZoneEdge(
  val chokepoint: Chokepoint,
  val zones:Iterable[Zone])

class Base(
  val townHallPosition:TileRectangle,
  val zone:Zone,
  val miningArea:TileRectangle)