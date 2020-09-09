package Macro.Architecture.Tiles

import Lifecycle.With
import Macro.Architecture.Blueprint
import Mathematics.Points.Tile

object TileSourceAnywhere extends TileSource {
  
  override def appropriateFor(blueprint: Blueprint): Boolean = (
    ! blueprint.requireTownHallTile.get
    && ! blueprint.requireGasTile.get
    && ! blueprint.requireCreep.get
    && ! blueprint.requirePower.get
  )
  
  override def tiles(blueprint: Blueprint): Iterable[Tile] = {
    With.geography.allTiles
  }
}
