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
  private var frame = 0

  @inline def advanceFrame(): Unit = { frame += 1 }

  def reset(): Unit = {
    populatedTiles.foreach(_.reset())
    populatedTiles.clear()
    frame += 2 // Expire requests even on cells that became empty before reset.
  }

  def populate(unit: Simulacrum): Unit = {
    val gridTile = tiles(unit.pixel.tile.clip.i)
    gridTile += unit
    unit.gridTile = Some(gridTile)
    populatedTiles += gridTile
  }

  @inline def remove(unit: Simulacrum): Unit = {
    unit.gridTile.foreach(tile => {
      tile -= unit
      if (tile.units.isEmpty) populatedTiles.remove(tile)
    })
    unit.gridTile = None
  }

  @inline def tryMove(unit: Simulacrum, to: Pixel): Unit = {
    val toClamped = to.clamp()
    if (unit.flying || unit.pixel.pixelDistanceSquared(toClamped) <= unit.topSpeed * unit.topSpeed || unit.pixel.tile == toClamped.tile) {
      val step = unit.pixel.projectUpTo(toClamped, unit.topSpeed)
      if (tiles(step.tile.i).fits(unit)) {
        relocateUnit(unit, step)
        return
      }
      tiles(step.tile.i).requestEntry(unit, frame)
    }
    if (tryForceV2(unit, unit.pixel.flowTo(toClamped))){
      return
    }
    if (moveGrid(unit, toClamped)) {
      return
    }
    val fail = "Fail!"
  }

  @inline private def moveGrid(unit: Simulacrum, to: Pixel): Boolean = {
    val from  = unit.pixel
    val fromTile = from.tile
    val path = fromTile
      .adjacent4
      .filter(_.walkable)
      .sortBy(t => (unit.lastTile.contains(t), t.groundTiles(to) + t.pixelDistanceSquared(to)))
      .map(toTile => from.add(32 * (toTile.x - fromTile.x), 32 * (toTile.y - fromTile.y)))
      .find(to => tryForceV2(unit, new Force(to.subtract(from)), requestSpace = false))

    if (path.isDefined) {
      true
    } else {
      false
    }
  }

  @inline def tryYield(unit: Simulacrum): Boolean = {
    if (unit.flying || ! unit.canMove || unit.topSpeed <= 0 || ! unit.behavior.fighting || unit.target.isEmpty || unit.gridTile.isEmpty) return false
    val current = unit.gridTile.get
    if ( ! current.requestedLastFrame(unit, frame)) return false
    val target = unit.target.get
    if ( ! target.alive) return false
    val from = unit.pixel
    val goal = unit.moveGoal.getOrElse(target.pixel)
    val forwardX = goal.x - from.x
    val forwardY = goal.y - from.y
    if (forwardX == 0 && forwardY == 0) return false
    val horizontal = Math.abs(forwardX) >= Math.abs(forwardY)
    val preserveRange = unit.inRangeToAttackSimulation(target)
    val range = unit.pixelRangeAgainst(target)
    var bestDirection = -1
    var bestDistance = Double.PositiveInfinity
    var direction = 0
    while (direction < 4) {
      val dx = if (direction == 2) -1 else if (direction == 3) 1 else 0
      val dy = if (direction == 0) -1 else if (direction == 1) 1 else 0
      val x = current.tile.x + dx
      val y = current.tile.y + dy
      val backwards = if (horizontal) dx * forwardX < 0 else dy * forwardY < 0
      if ( ! backwards && x >= 0 && y >= 0 && x < With.mapTileWidth && y < With.mapTileHeight) {
        val destination = tiles(x + y * With.mapTileWidth)
        // Staying in a requested cell costs more than moving toward an unrequested neighbor.
        if ( ! destination.requestedLastFrame(unit, frame) && destination.fits(unit)) {
          val stepX = Maff.clamp(from.x + (dx * unit.topSpeed).toInt, 0, With.mapPixelWidth - 1)
          val stepY = Maff.clamp(from.y + (dy * unit.topSpeed).toInt, 0, With.mapPixelHeight - 1)
          val distance = Maff.broodWarDistanceBox(
            stepX - unit.unitClass.dimensionLeft, stepY - unit.unitClass.dimensionUp,
            stepX + unit.unitClass.dimensionRightExclusive, stepY + unit.unitClass.dimensionDownExclusive,
            target.pixelLeft, target.pixelTop, target.pixelRightExclusive, target.pixelBottomExclusive)
          if ( ! preserveRange || (distance <= range && (unit.pixelRangeMin <= 0 || distance > unit.pixelRangeMin))) {
            val goalDistance = Maff.squared(32 * x + 16 - goal.x) + Maff.squared(32 * y + 16 - goal.y)
            if (goalDistance < bestDistance) {
              bestDistance = goalDistance
              bestDirection = direction
            }
          }
        }
      }
      direction += 1
    }
    if (bestDirection < 0) return false
    val dx = if (bestDirection == 2) -1 else if (bestDirection == 3) 1 else 0
    val dy = if (bestDirection == 0) -1 else if (bestDirection == 1) 1 else 0
    tryForceV2(unit, Force(dx, dy), requestSpace = false)
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

  @inline def relocateUnit(unit: Simulacrum, to: Pixel, knownTile: Option[SimulationGridTile] = None): Unit = {
    val tileFrom  = unit.gridTile
    val tileTo    = knownTile.getOrElse(tiles(to.tile.clip.i))

    if ( ! unit.gridTile.contains(tileTo)) {
      unit.gridTile.foreach(tileLast => {
        tileLast -= unit
        if (tileLast.units.isEmpty) {
          populatedTiles.remove(tileLast)
        }
      })
      if (tileTo.units.isEmpty) {
        populatedTiles.add(tileTo)
      }
      tileTo += unit
      unit.lastTile = tileFrom.map(_.tile)
      unit.gridTile = Some(tileTo)
    }
    unit.setPixel(to)
  }

  @inline def tryForceV2(unit: Simulacrum, force: Force, requestSpace: Boolean = true): Boolean = {
          val from      = unit.pixel
          val forceStep = force.normalize(unit.topSpeed)
          val toStep    = from.add(forceStep.x.toInt, forceStep.y.toInt).clamp()
          val tileStep  = tiles(toStep.tile.i)
          val accept    = toStep != from && tileStep.fits(unit)

    if (accept) {
      relocateUnit(unit, toStep, Some(tileStep))
    } else if (requestSpace && toStep != from) {
      tileStep.requestEntry(unit, frame)
    }
    accept
  }

  @inline def tryForce(unit: Simulacrum, force: Force): Boolean = {
          val from      = unit.pixel
          val to        = from.add((unit.topSpeed * force.x).toInt, (unit.topSpeed * force.y).toInt).clamp()
          val tile      = to.tile
    lazy  val gridTile  = tiles(tile.i)
          val accept    = gridTile.fits(unit)
    if (accept) {
      relocateUnit(unit, to)
    }
    accept
  }
}
