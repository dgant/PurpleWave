package Placement.Generation

import Information.Geography.Types.Zone
import Mathematics.Points.Tile

import scala.collection.immutable.VectorIterator

class TileGeneratorProximity(origin: Tile, zone: Zone) extends TileGenerator {
  val tiles   : Vector[Tile]         = zone.tiles.toVector.sortBy(_.tileDistanceSquared(origin))
  val iterator: VectorIterator[Tile] = tiles.iterator

  override def next(): Tile = iterator.next()
  override def hasNext: Boolean = iterator.hasNext
}
