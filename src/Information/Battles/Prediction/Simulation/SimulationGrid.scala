package Information.Battles.Prediction.Simulation

import Lifecycle.With
import Mathematics.Points.Pixel
import ProxyBwapi.Races.Protoss

final class SimulationGrid {
  val occupancyMax: Int = Protoss.Dragoon.area
  val tiles: Array[SimulationGridTile] = (0 until With.mapTileArea).map(new SimulationGridTile(_)).toArray

  @inline def tryMove(unit: NewSimulacrum, to: Pixel): Boolean = {
    // TODO: Optimize these checks to avoid creating new pixels/tiles
    // TODO: Check walkability
    // TODO: Walk around obstacles
    lazy val toGridTile = tiles(to.tile.i)
    if (unit.gridTile.tile.contains(to) || (to.valid && toGridTile.occupancy + unit.unitClass.occupancy <= occupancyMax)) {
      unit.gridTile -= unit
      unit.pixel = to
      unit.gridTile = toGridTile
      unit.gridTile += unit
      true
    } else {
      false
    }
  }
}
