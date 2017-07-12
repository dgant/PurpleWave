package Macro.Architecture.Tiles

import Lifecycle.With
import Macro.Architecture.Blueprint
import Mathematics.Points.Tile

object TileSourceOurZones extends TileSource {
  
  override def appropriateFor(blueprint: Blueprint): Boolean = {
    true
  }
  
  override def tiles(blueprint: Blueprint): Iterable[Tile] = {
    With.geography.ourZones.flatMap(_.tilesBuildable)
  }
}
