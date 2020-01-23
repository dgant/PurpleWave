package Macro.Architecture.Tiles
import Lifecycle.With
import Macro.Architecture.Blueprint
import Mathematics.Points.Tile
import ProxyBwapi.Races.Neutral

object TileSourceGas extends TileSource {
  
  override def appropriateFor(blueprint: Blueprint): Boolean = {
    blueprint.requireGasTile.get
  }
  
  override def tiles(blueprint: Blueprint): Seq[Tile] = {
    With.geography.bases
      .view
      .filter(_.townHall.exists(_.player.isUs))
      .flatMap(_.gas.filter(_.unitClass == Neutral.Geyser).map(_.tileTopLeft))
  }
}
