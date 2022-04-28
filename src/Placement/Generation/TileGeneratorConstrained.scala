package Placement.Generation
import Information.Geography.Types.Base
import Mathematics.Points.Tile
import Placement.Templating.Template

class TileGeneratorConstrained(base: Base, template: Template) extends TileGenerator {
  private val relativeHalls = template.points.view.filter(_.requirement.isTownHall).map(_.point)
  private val relativeGas   = template.points.view.filter(_.requirement.isGas).map(_.point)
  private val queueHalls    = relativeHalls.map(p => base.townHallTile.subtract(p.x, p.y))
  private val queueGas      = relativeGas.flatMap(p => base.gas.map(_.tileTopLeft.subtract(p.x, p.y)))
  private val queue         = (queueHalls ++ queueGas).toVector
  private var i = 0

  override def next(): Tile = {
    i += 1
    queue(i - 1)
  }

  override def hasNext: Boolean = i < queue.length
}
