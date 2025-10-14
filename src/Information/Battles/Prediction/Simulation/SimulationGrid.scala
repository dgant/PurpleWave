package Information.Battles.Prediction.Simulation

import Lifecycle.With
import Mathematics.Physics.Force
import Mathematics.Points.Pixel
import ProxyBwapi.Races.Protoss
import Mathematics.Maff

import scala.collection.mutable

final class SimulationGrid {
  val occupancyMax: Int = Protoss.Dragoon.area
  val tiles: Array[SimulationGridTile] = (0 until With.mapTileArea).map(new SimulationGridTile(_)).toArray

  val populatedTiles: mutable.Set[SimulationGridTile] = new mutable.HashSet[SimulationGridTile]

  def reset(): Unit = {
    populatedTiles.foreach(_.reset())
    populatedTiles.clear()
  }

  def populate(unit: Simulacrum): Unit = {
    val gridTile = tiles(unit.pixel.tile.clip.i)
    gridTile += unit
    unit.gridTile = Some(gridTile)
    populatedTiles += gridTile
  }

  @inline def tryMove(unit: Simulacrum, to: Pixel): Unit = {
    val toClamped = to.clamp()
    if (unit.pixel.tile == toClamped.tile) {
      unit.pixel = toClamped
      return
    }
    if (unit.flying) {
      unit.pixel = toClamped
      move(unit, tiles(toClamped.tile.i))
      return
    }
    moveGrid(unit, toClamped)
  }

  @inline private def moveGrid(unit: Simulacrum, to: Pixel): Unit = {
    val from  = unit.pixel
    val path = from
      .tile
      .adjacent4
      .filter(_.walkable)
      .sortBy(_.groundTiles(to))
      .find(toTile => tryForce(unit, from.flowTo(toTile.center.add(from.offsetFromTileCenter))))
  }

  @inline private def moveFlow(unit: Simulacrum, to: Pixel): Unit = {

    val from  = unit.pixel
    val force = from.flowTo(to)

    // Choose the rotation direction (+90 or -90) that brings us closer to the goal direction
    lazy val targetRadians = from.radiansTo(to)
    lazy val fRad          = force.radians
    lazy val dPlus         = Math.abs(Maff.radiansTo(fRad + Maff.halfPi, targetRadians))
    lazy val dMinus        = Math.abs(Maff.radiansTo(fRad - Maff.halfPi, targetRadians))
    lazy val direction     = if (dPlus <= dMinus) 1 else -1 // Either -1 or 1; prefer the closer rotation

    if (tryForce(unit, force)) return

    var path = from
      .tile
      .adjacent4
      .filter(_.walkable)
      .sortBy(_.groundTiles(to))
      .find(toTile => tryForce(unit, from.flowTo(toTile.center)))

    if (path.isEmpty) {
      path = None
    }

      /*
    if (tryForce(unit, force.rotate(  45  * direction)))  return
    if (tryForce(unit, force.rotate(- 45  * direction)))  return
    if (tryForce(unit, force.rotate(  90  * direction)))  return
    if (tryForce(unit, force.rotate(- 90  * direction)))  return
    if (tryForce(unit, force.rotate(  135 * direction)))  return
    if (tryForce(unit, force.rotate(- 135 * direction)))  return
    if (tryForce(unit, force.rotate(180)))                return
    */
  }

  @inline def move(unit: Simulacrum, tileNext: SimulationGridTile): Unit = {
    unit.gridTile.foreach(tileLast => {
      tileLast -= unit
      if (tileLast.units.isEmpty) {
        populatedTiles.remove(tileNext)
      }
    })
    if (tileNext.units.isEmpty) {
      populatedTiles.add(tileNext)
    }
    tileNext += unit
    unit.gridTile = Some(tileNext)
  }

  @inline def tryForce(unit: Simulacrum, force: Force): Boolean = {
          val to        = unit.pixel.add((unit.topSpeed * force.x).toInt, (unit.topSpeed * force.y).toInt).clamp()
          val tile      = to.tile
    lazy  val gridTile  = tiles(tile.i)
          val accept    = gridTile.fits(unit)
    if (accept) {
      unit.pixel = to
      move(unit, gridTile)
    }
    accept
  }
}
