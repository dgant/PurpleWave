package Macro.Architecture.Tiles
import Lifecycle.With
import Macro.Architecture.Blueprint
import Mathematics.Points.Tile

object TileSourcePreplacementSpecific extends TileSource {

  override def appropriateFor(blueprint: Blueprint): Boolean = true

  override def tiles(blueprint: Blueprint): Seq[Tile] = {
    With.preplacement.preplacement.get(blueprint.building).view.filter(filter)
  }

  def filter(tile: Tile): Boolean = {
    tile.zone.bases.exists(_.owner.isUs) || tile.zone.bases.exists(_.isNaturalOf.exists(_.owner.isUs))
  }
}
