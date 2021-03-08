package Macro.Architecture.Tiles

import Lifecycle.With
import Macro.Architecture.Blueprint
import Mathematics.Points.Tile
import ProxyBwapi.Races.Protoss

object TileSourceOurPylons extends TileSource {
  
  override def appropriateFor(blueprint: Blueprint): Boolean = {
    blueprint.requirePower.contains(true)
  }
  
  override def tiles(blueprint: Blueprint): Iterable[Tile] = {
    With.units.ours
      .filter(Protoss.Pylon)
      .flatMap(pylon =>
        (if (blueprint.heightTiles.contains(2))
          With.grids.psi2Height.psiPoints
        else
          With.grids.psi3Height.psiPoints)
        .map(pylon.tileTopLeft.add))
  }
}
