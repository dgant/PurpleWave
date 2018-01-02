package Macro.Architecture.Tiles

import Lifecycle.With
import Macro.Architecture.Blueprint
import Mathematics.Points.Tile

object TileSourceAnywhere extends TileSource {
  
  override def appropriateFor(blueprint: Blueprint): Boolean = {
    true
  }
  
  override def tiles(blueprint: Blueprint): Iterable[Tile] = {
    With.geography.allTiles.filter(With.grids.buildable.get)
  }
}
