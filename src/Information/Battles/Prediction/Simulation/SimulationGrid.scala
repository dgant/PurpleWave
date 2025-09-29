package Information.Battles.Prediction.Simulation

import Lifecycle.With
import Mathematics.Physics.Force
import Mathematics.Points.Pixel
import ProxyBwapi.Races.Protoss
import Utilities.?

final class SimulationGrid {
  val occupancyMax: Int = Protoss.Dragoon.area
  val tiles: Array[SimulationGridTile] = (0 until With.mapTileArea).map(new SimulationGridTile(_)).toArray

  @inline def tryMove(unit: Simulacrum, to: Pixel): Unit = {
    val from    = unit.pixel
    val force   = ?(unit.flying, new Force(to.subtract(from)).normalize, from.flowTo(to))
    val end     = from.add((unit.topSpeed * force.x).toInt, (unit.topSpeed * force.y).toInt).clamp()
    unit.pixel  = end

    // TODO: Optimize these checks to avoid creating new pixels/tiles
    // TODO: Check walkability
    // TODO: Walk around obstacles
    //if (unit.gridTile.tile.contains(to) || (to.valid && tiles(to.tile.i).occupancy + unit.unitClass.occupancy <= occupancyMax)) {
    //unit.gridTile = tiles(to.tile.clip.i)
    //unit.gridTile += unit
  }
}
