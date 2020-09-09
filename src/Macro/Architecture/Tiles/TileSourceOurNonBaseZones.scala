package Macro.Architecture.Tiles

import Lifecycle.With
import Macro.Architecture.Blueprint
import Mathematics.Points.Tile

object TileSourceOurNonBaseZones extends TileSource {
  
  override def appropriateFor(blueprint: Blueprint): Boolean = TileSourceAnywhere.appropriateFor(blueprint)
  
  override def tiles(blueprint: Blueprint): Iterable[Tile] = {
    With.geography.ourZones.view.filterNot(_.bases.exists(_.owner.isUs)).flatMap(_.tilesBuildable.view)
  }
}
