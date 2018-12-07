package Macro.Architecture.Tiles
import Macro.Architecture.Blueprint
import Mathematics.Points.Tile

object TileSourceBlueprint extends TileSource {
  
  override def appropriateFor(blueprint: Blueprint): Boolean = {
    blueprint.requireCandidates.isDefined
  }
  
  override def tiles(blueprint: Blueprint): Seq[Tile] = {
    blueprint.requireCandidates.get
  }
}
