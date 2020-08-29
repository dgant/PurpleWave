package Macro.Architecture.Tiles

import Macro.Architecture.Blueprint

object Surveyor {
  
  val tileSources = Seq(
    Seq(
      TileSourcePreplacement),
    Seq(
      TileSourceBlueprint,
      TileSourceBlueprintZoneRequired,
      TileSourceBlueprintZonePreferred),
    Seq(
      TileSourcePlasma,
      TileSourceTownHall,
      TileSourceGas,
      TileSourceOurBases,
      TileSourceOurNonBaseZones,
      TileSourceOurPylons),
    Seq(
      TileSourceAnywhere))
  
  def candidates(blueprint: Blueprint): Seq[Seq[TileSource]] = {
    tileSources.map(_.filter(_.appropriateFor(blueprint)))
  }
}
