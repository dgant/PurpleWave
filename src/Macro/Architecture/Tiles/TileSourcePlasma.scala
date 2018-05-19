package Macro.Architecture.Tiles
import Lifecycle.With
import Macro.Architecture.Blueprint
import Mathematics.Points.Tile
import Mathematics.Shapes.Spiral

object TileSourcePlasma extends TileSource {
  
  override def appropriateFor(blueprint: Blueprint): Boolean = {
    With.strategy.isPlasma
  }
  
  override def tiles(blueprint: Blueprint): Iterable[Tile] = {
    if (blueprint.requireTownHallTile.get) {
      With.geography.bases
        .toVector
        .sortBy(_.heart.tileDistanceFast(With.self.startTile))
        .take(3)
        .map(_.townHallArea.startInclusive)
    }
    else if (blueprint.requireGasTile.get) {
      With.geography.bases
        .filter(_.townHall.exists(_.player.isUs))
        .flatMap(_.gas.map(_.tileTopLeft))
    }
    else {
      With.geography.ourBases.flatMap(base =>
        Spiral
          .points(18)
          .map(base.heart.add)
          .filter(_.valid))
    }
  }
}
