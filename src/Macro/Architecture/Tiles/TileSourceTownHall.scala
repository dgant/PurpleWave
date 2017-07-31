package Macro.Architecture.Tiles
import Lifecycle.With
import Macro.Architecture.Blueprint
import Mathematics.Points.Tile

object TileSourceTownHall extends TileSource {
  
  override def appropriateFor(blueprint: Blueprint): Boolean = {
    blueprint.requireTownHallTile.get
  }
  
  override def tiles(blueprint: Blueprint): Iterable[Tile] = {
    With.geography.bases
      .filterNot(base => base.owner.isEnemy || base.zone.island)
      .map(_.townHallArea.startInclusive)
  }
}
