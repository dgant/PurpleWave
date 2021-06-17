package Planning.Plans.Placement

import Lifecycle.With
import Mathematics.Points.{Point, Tile}
import Mathematics.Maff
import Planning.Plan
import ProxyBwapi.Races.Protoss
import ProxyBwapi.UnitClasses.UnitClass

class LayoutWall extends Plan {

  var layout: Option[Seq[(UnitClass, Tile)]] = None

  override def onUpdate(): Unit = {
    if (layout.isEmpty) {
      layout = Some(calculate())
    }
    layout.get.foreach{case (unitClass, tile) => With.groundskeeper.suggest(unitClass, tile)}
  }

  def wallBuildings: Seq[UnitClass] = Seq(Protoss.Gateway, Protoss.Forge)

  def calculate(): Seq[(UnitClass, Tile)] = {
    val main = With.geography.ourMain
    val natural = With.geography.ourNatural
    if (natural.zone.exit.isEmpty) return Seq.empty
    if (main.zone.exit.isEmpty) return Seq.empty

    val entrance = main.zone.exit.get.pixelCenter.tile
    val exit = natural.zone.exit.get.pixelCenter.tile
    val origin = natural.townHallArea.tiles.minBy(t => t.tileDistanceManhattan(entrance) + t.tileDistanceManhattan(exit))

    val exitDirection = exit.subtract(origin).direction
    val wallDirectionAbs = Point(
      Math.abs(exitDirection.y),
      Math.abs(exitDirection.x))
    val wallDirection = Point(
      Maff.signum(origin.subtract(entrance).x) * wallDirectionAbs.x,
      Maff.signum(origin.subtract(entrance).y) * wallDirectionAbs.y)

    case class Span(var distance: Int, var seed: Tile, var start: Tile, var end: Tile) {
      def length: Int = start.tileDistanceManhattan(end)
    }
    def makeSpan(distance: Int) = {
      val seed = origin.add(distance * exitDirection.x, distance * exitDirection.y)
      def expand(from: Tile, direction: Point): Tile = {
        var best = from
        var next = from
        while (next.valid && With.grids.buildable.get(next)) {
          best = next
          next = next.add(direction)
        }
        best
      }
      val start = expand(seed, Point(-wallDirection.x, -wallDirection.y))
      val end = expand(seed, wallDirection)
      Span(distance, seed, start, end)
    }

    val spans = (1 to Math.min(12, 2 + exit.subtract(origin).maxDimensionLength)).map(makeSpan)

    //TODO
    Seq.empty
  }
}
