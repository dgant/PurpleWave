package Macro.Architecture.Tiles

import Macro.Architecture.Blueprint

object Surveyor {
  
  val tileSources = Array(
    TileSourceBlueprint,
    TileSourceBlueprintZoneRequired,
    TileSourceBlueprintZonePreferred,
    TileSourcePlasma,
    TileSourceTownHall,
    TileSourceGas,
    TileSourceOurBases,
    TileSourceOurNonBaseZones,
    TileSourceOurPylons,
    TileSourceAnywhere)
  
  def candidates(blueprint: Blueprint): Array[TileSource] = {
    tileSources.filter(_.appropriateFor(blueprint))
  }
}
