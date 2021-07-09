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
      .view
      .filter(b => b.mineralsLeft > 3000 || b.gasLeft > 1000)
      .filterNot(base => base.owner.isEnemy || base.zone.island || (With.strategy.isIslandMap && With.geography.home.pixelDistanceGround(base.townHallTile) > With.mapPixelWidth * 4))
      .map(_.townHallTile)
  }
}
