package Strategies.PositionFinders
import Startup.With
import bwapi.TilePosition

class PositionCenter extends PositionFinder {
  
  val _center = new TilePosition(
      With.game.mapWidth / 2,
      With.game.mapHeight / 2)
  
  override def find(): Option[TilePosition] = Some(_center)
}
