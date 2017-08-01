package Macro.Architecture.Tiles

import Macro.Architecture.Blueprint
import Mathematics.Points.Tile

object TileSourceBlueprintZonePreferred extends TileSource {
  
  override def appropriateFor(blueprint: Blueprint): Boolean = {
    blueprint.preferZone.isDefined
  }
  
  override def tiles(blueprint: Blueprint): Iterable[Tile] = {
    blueprint.preferZone.get.tilesBuildable
  }
}
