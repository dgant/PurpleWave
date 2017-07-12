package Macro.Architecture.Tiles

import Macro.Architecture.Blueprint

object Surveyor {
  
  val tileSources = Array(
    TileSourceBlueprint,
    TileSourceBlueprintZone,
    TileSourcePlasma,
    TileSourceTownHall,
    TileSourceGas,
    TileSourceOurBases,
    TileSourceOurNonBaseZones,
    TileSourceOurPylons,
    TileSourceAnywhere)
  
  def candidates(blueprint: Blueprint): Iterable[TileSource] = {
    tileSources.filter(_.appropriateFor(blueprint))
  }
}
