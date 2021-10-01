package Macro.Architecture.Tiles

import Lifecycle.With
import Macro.Architecture.Blueprint
import Mathematics.Points.Tile
import ProxyBwapi.Races.Protoss

object TileSourceOurNonBaseZones extends TileSource {
  
  override def appropriateFor(blueprint: Blueprint): Boolean = TileSourceAnywhere.appropriateFor(blueprint)
  
  override def tiles(blueprint: Blueprint): Iterable[Tile] = {
    (With.geography.ourZones.view ++ With.geography.bases.flatMap(_.metro.zones))
      .filterNot(_.bases.exists(_.owner.isUs))
      .toVector
      .distinct
      .view
      .filterNot(_.bases.exists(_.townHallArea.tiles.exists(_.units.exists(u => u.isOurs && Protoss.Pylon(u)))))
      .flatMap(_.tilesBuildable)
  }
}
