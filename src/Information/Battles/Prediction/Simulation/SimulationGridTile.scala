package Information.Battles.Prediction.Simulation

import Mathematics.Points.Tile
import ProxyBwapi.UnitTracking.UnorderedBuffer

final class SimulationGridTile(val i: Int) {
  val tile: Tile = new Tile(i)
  val units = new UnorderedBuffer[Simulacrum](12)
  var occupancy: Int = 0

  @inline def reset(): Unit = {
    units.clear()
    occupancy = 0
  }

  @inline def +=(unit: Simulacrum): Unit = {
    units.add(unit)
    occupancy += unit.unitClass.occupancy
  }
  @inline def -=(unit: Simulacrum): Unit = {
    units.remove(unit)
    occupancy -= unit.unitClass.occupancy
  }
  @inline def fits(unit: Simulacrum): Boolean = {
    if (unit.gridTile.contains(this)) {
      return true
    }

    if ( ! unit.flying && ! tile.walkable) {
      return false
    }

    occupancy + unit.unitClass.occupancy < Occupancy.Resolution || unit.target.exists(_.gridTile.contains(this))
  }
}
