package Placement.Generation
import Information.Geography.Types.Zone
import Mathematics.Points.Tile
import Mathematics.Shapes.Spiral

class TileGeneratorSpiral(origin: Tile, zone: Zone) extends TileGenerator {
  private val iterator = Spiral(Math.ceil(zone.boundary.diagonal).toInt).iterator

  private var nextNext: Tile = origin
  private var atEnd: Boolean = false

  private def advance(): Unit = {
    do {
      if (iterator.hasNext) {
        nextNext = origin.add(iterator.next())
      } else {
        atEnd = true
      }
    } while (nextNext.zone != zone && iterator.hasNext)
  }

  advance()

  override def next(): Tile = {
    val output = nextNext
    advance()
    output
  }

  override def hasNext: Boolean = ! atEnd
}
