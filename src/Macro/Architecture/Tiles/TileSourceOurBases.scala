package Macro.Architecture.Tiles
import Lifecycle.With
import Macro.Architecture.Blueprint
import Mathematics.Points.Tile

object TileSourceOurBases extends TileSource {
  
  override def appropriateFor(blueprint: Blueprint): Boolean = TileSourceAnywhere.appropriateFor(blueprint)
  
  override def tiles(blueprint: Blueprint): Iterable[Tile] = {
    With.geography.ourBases.view.flatMap(_.zone.tilesBuildable)
  }
}
