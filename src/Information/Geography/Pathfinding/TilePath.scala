package Information.Geography.Pathfinding

import Mathematics.Points.Tile

case class TilePath(
  start     : Tile,
  end       : Tile,
  distance  : Int,
  visited   : Int,
  tiles     : Option[Iterable[Tile]]) {
  
  def pathExists: Boolean = tiles.isDefined
}