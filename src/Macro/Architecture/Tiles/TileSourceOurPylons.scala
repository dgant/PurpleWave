package Macro.Architecture.Tiles

import Lifecycle.With
import Macro.Architecture.Blueprint
import Mathematics.Points.Tile
import ProxyBwapi.Races.Protoss

import scala.collection.mutable

object TileSourceOurPylons extends TileSource {
  
  override def appropriateFor(blueprint: Blueprint): Boolean = {
    blueprint.requirePower.get
  }
  
  override def tiles(blueprint: Blueprint): Seq[Tile] = {
    val output = new mutable.ArrayBuffer[Tile]
    With.units.ours
      .foreach(pylon =>
        if (pylon.is(Protoss.Pylon))
          With.grids.psi3Height.psiPoints.foreach(point =>
            pylon.tileTopLeft.add(point)
          ))
    output.distinct
  }
}
