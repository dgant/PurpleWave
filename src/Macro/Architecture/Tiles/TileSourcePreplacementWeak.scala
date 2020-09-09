package Macro.Architecture.Tiles

import Lifecycle.With
import Macro.Architecture.Blueprint
import Mathematics.Points.Tile

object TileSourcePreplacementWeak extends TileSource {

  override def appropriateFor(blueprint: Blueprint): Boolean = true

  override def tiles(blueprint: Blueprint): Iterable[Tile] = {
    val dimensions = (blueprint.building.tileWidth, blueprint.building.tileHeight)

    val reduced = dimensions match {
      // Defiler Nest is 4x2 but we don't want to stick it in a 4x3 due to likelihood of creating a trap.
      case (3, 2)   => Seq((4, 3), (4, 2))
      case (2, 2)   => Seq((4, 3), (4, 2), (3, 2))
      case default  => Seq.empty
    }
    val unfiltered = reduced.flatMap(d => With.preplacement.preplacement.get(d._1, d._2).view)
    unfiltered.filter(TileSourcePreplacementSpecific.filter)
  }
}
