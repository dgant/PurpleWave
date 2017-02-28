package Geometry

import Startup.With
import bwapi.{Position, TilePosition}

object Positions {
  val middle = new Position(
    With.game.mapWidth * 32 / 2,
    With.game.mapHeight * 32 / 2)
  
  val tileMiddle = new TilePosition(
      With.game.mapWidth / 2,
      With.game.mapHeight / 2)
}
