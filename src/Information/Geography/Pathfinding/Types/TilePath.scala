package Information.Geography.Pathfinding.Types

import Mathematics.Points.Tile

case class TilePath(
  start     : Tile,
  end       : Tile,
  distance  : Int,
  tiles     : Option[IndexedSeq[Tile]]) {
  
  def pathExists: Boolean = tiles.isDefined
}