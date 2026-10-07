package Information.Battles.Prediction.Simulation

import Mathematics.Points.Tile
import ProxyBwapi.UnitTracking.UnorderedBuffer
import Utilities.?

final class SimulationGridTile(val i: Int) {
  val tile: Tile = new Tile(i)
  val units = new UnorderedBuffer[Simulacrum](12)
  var occupancy: Int = 0
  private var demandFrameEven = -1
  private var demandFrameOdd  = -1
  private var demandSidesEven = 0
  private var demandSidesOdd  = 0

  @inline def requestEntry(unit: Simulacrum, frame: Int): Unit = {
    if (unit.flying || unit.gridTile.contains(this) || ! tile.walkable) return
    val side = if (unit.isFriendlyCached) 1 else 2
    if ((frame & 1) == 0) {
      if (demandFrameEven != frame) demandSidesEven = 0
      demandFrameEven = frame
      demandSidesEven |= side
    } else {
      if (demandFrameOdd != frame) demandSidesOdd = 0
      demandFrameOdd = frame
      demandSidesOdd |= side
    }
  }

  @inline def requestedLastFrame(unit: Simulacrum, frame: Int): Boolean = {
    val previous = frame - 1
    val side = if (unit.isFriendlyCached) 1 else 2
    if ((previous & 1) == 0) demandFrameEven == previous && (demandSidesEven & side) != 0
    else demandFrameOdd == previous && (demandSidesOdd & side) != 0
  }

  @inline def reset(): Unit = {
    units.clear()
    occupancy = 0
  }

  @inline def +=(unit: Simulacrum): Unit = {
    units.add(unit)
    occupancy += unit.occupancy
  }
  @inline def -=(unit: Simulacrum): Unit = {
    units.remove(unit)
    occupancy -= unit.occupancy
  }
  @inline def fits(unit: Simulacrum): Boolean = {
    if (unit.flying) {
      return true
    }
    if (unit.gridTile.contains(this)) {
      return true
    }
    if ( ! tile.walkable) {
      return false
    }

    val occupancyCost = ?(unit.target.exists(_.gridTile.contains(this)), 0, unit.occupancy)
    if (occupancy + occupancyCost > Occupancy.Resolution) {
      return false
    }

    true
  }
}
