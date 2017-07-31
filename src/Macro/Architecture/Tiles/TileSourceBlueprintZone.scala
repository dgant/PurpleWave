package Macro.Architecture.Tiles
import Macro.Architecture.Blueprint
import Mathematics.Points.Tile

object TileSourceBlueprintZone extends TileSource {
  
  override def appropriateFor(blueprint: Blueprint): Boolean = {
    blueprint.requireZone.isDefined
  }
  
  override def tiles(blueprint: Blueprint): Iterable[Tile] = {
    blueprint.requireZone.get.tilesBuildable
  }
}
