package Micro.Actions.Combat.Maneuvering

import Mathematics.Points.Tile
import Mathematics.Shapes.Ring
import Micro.Coordination.Pathing.MicroPathing

import scala.collection.mutable.ArrayBuffer

/**
  * Performance-optimized greedy pathfinder.
  * Can only find paths which are strictly downhill in terms of estimated ground distance
  */
object DownhillPathfinder {
  def decend(from: Tile, to: Tile): Option[Iterable[Tile]] = {
    val directions = Ring.points(1)
    var firstDirection = 0 // Rotate the first direction we try to discover diagonals

    // Pre-allocating is hopefully a performance improvement
    val path = new ArrayBuffer[Tile](MicroPathing.waypointDistanceTiles)
    path += from

    def distance(tile: Tile): Double = tile.pixelDistanceGround(to)
    def here = path.last

    while (path.length < MicroPathing.waypointDistanceTiles) {
      if (here == to) return Some(path)
      var bestDistance = distance(here)
      var bestTile = here
      var iDirection = 0
      while (iDirection < 4) {
        val there = here.add(directions((firstDirection + iDirection) % 4))
        if (there.walkable && ! path.contains(there)) {
          val distanceThere = distance(there)
          if (distanceThere < bestDistance) {
            bestDistance = distanceThere
            bestTile = there
          }
        }
        iDirection += 1
      }
      if (bestTile == here) return None
      path += bestTile
      firstDirection = iDirection + 1
    }
    Some(path)
  }
}