package Macro.Architecture.Tiles
import Lifecycle.With
import Macro.Architecture.Blueprint
import Mathematics.Points.Tile

object TileSourcePreplacement extends TileSource {

  override def appropriateFor(blueprint: Blueprint): Boolean = true

  override def tiles(blueprint: Blueprint): Seq[Tile] = {
    With.preplacement.preplacement.get(blueprint.building).view ++ With.preplacement.preplacement.get(blueprint.building.tileWidth, blueprint.building.tileHeight).view
  }
}
