package Information.Battles.Prediction.Simulation

import Lifecycle.With
import Mathematics.Points.Pixel
import ProxyBwapi.Races.Protoss

final class SimulationGrid {
  val occupancyMax: Int = Protoss.Dragoon.area
  val tiles: Array[SimulationGridTile] = (0 until With.mapTileArea).map(new SimulationGridTile(_)).toArray

  @inline def tryMove(unit: Simulacrum, to: Pixel): Boolean = {
    // TODO: Optimize these checks to avoid creating new pixels/tiles
    // TODO: Check walkability
    // TODO: Walk around obstacles
    //if (unit.gridTile.tile.contains(to) || (to.valid && tiles(to.tile.i).occupancy + unit.unitClass.occupancy <= occupancyMax)) {
      unit.gridTile -= unit
      unit.pixel = to
      unit.gridTile = tiles(to.tile.i)
      unit.gridTile += unit
      true
    //} else {
    //  false
    //}
  }
}
