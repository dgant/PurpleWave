package Macro.Architecture.Tiles

import Lifecycle.With
import Macro.Architecture.Blueprint
import Mathematics.Points.Tile

object TileSourcePreplacementWeak extends TileSource {

  override def appropriateFor(blueprint: Blueprint): Boolean = true

  override def tiles(blueprint: Blueprint): Seq[Tile] = {
    val dimensions = (blueprint.building.tileWidth, blueprint.building.tileHeight)

    val reduced = dimensions match {
      case (4, 2)   => Seq((4, 3))
      case (3, 2)   => Seq((4, 3))
      case (3, 2)   => Seq((4, 3), (4, 2))
      case (2, 2)   => Seq((4, 3), (4, 2), (3, 2))
      case default  => Seq.empty
    }
    val unfiltered = reduced.flatMap(d => With.preplacement.preplacement.get(d._1, d._2).view)
    unfiltered.filter(TileSourcePreplacementSpecific.filter)
  }
}
