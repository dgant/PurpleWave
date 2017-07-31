package Macro.Architecture.Tiles

import Lifecycle.With
import Macro.Architecture.Blueprint
import Mathematics.Points.Tile
import ProxyBwapi.Races.Protoss

object TileSourceOurPylons extends TileSource {
  
  override def appropriateFor(blueprint: Blueprint): Boolean = {
    blueprint.requirePower.get
  }
  
  override def tiles(blueprint: Blueprint): Iterable[Tile] = {
    With.grids.psi3Height.psiPoints.flatMap(point =>
      With.units.ours
      .filter(_.is(Protoss.Pylon))
      .map(_.tileTopLeft.add(point)))
  }
}
