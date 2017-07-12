package Macro.Architecture.Tiles
import Lifecycle.With
import Macro.Architecture.Blueprint
import Mathematics.Points.Tile

object TileSourceGas extends TileSource {
  override def appropriateFor(blueprint: Blueprint): Boolean = {
    blueprint.gas
  }
  
  override def tiles(blueprint: Blueprint): Iterable[Tile] = {
    With.geography.bases
      .filter(_.townHall.exists(_.player.isUs))
      .flatMap(_.gas.map(_.tileTopLeft))
  }
}
