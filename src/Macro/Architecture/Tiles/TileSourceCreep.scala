package Macro.Architecture.Tiles
import Lifecycle.With
import Macro.Architecture.Blueprint
import Mathematics.Points.{Tile, TileRectangle}
import Utilities.UnitFilters.{IsHatchlike, IsAny}
import ProxyBwapi.Races.Zerg

object TileSourceCreep extends TileSource {
  override def appropriateFor(blueprint: Blueprint): Boolean = blueprint.requireCreep.contains(true)

  override def tiles(blueprint: Blueprint): Iterable[Tile] = {
    With.units.ours
      .filter(IsAny(IsHatchlike, Zerg.CreepColony, Zerg.SunkenColony, Zerg.SporeColony))
      .map(_.tileTopLeft)
      .flatMap(t => TileRectangle(t.subtract(9, 5), t.add(10, 6)).tiles)
      .filter(_.creep) // Not unchecked, because we're also testing validity
  }
}
