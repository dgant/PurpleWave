package Information.Geography.Pathfinding

import Mathematics.Points.{Pixel, Tile}
import Utilities.ByOption

trait GroundDistance {

  val impossiblyLargeDistance: Long = 32L * 32L * 256L * 256L * 100L

  @inline final def groundPathExists(origin: Tile, destination: Tile): Boolean = {
    origin.zone == destination.zone || groundTiles(origin, destination) < impossiblyLargeDistance
  }

  @inline final def groundTilesManhattan(origin: Tile, destination: Tile): Long = {
    // Let's first check if we can use air distance. It's cheaper and more accurate.
    // We can "get away" with using air distance if we're in the same zone
    if (origin.zone == destination.zone) {
      return origin.tileDistanceManhattan(destination)
    }

    // This approximation -- calculating ground distance at tile resolution -- can potentially bite us.
    // Pun intended on "potentially" -- the risk here is using it for potential fields near a chokepoint
    // before which we're getting pixel-resolution distance and after which we're getting tile-resolution distance
    Math.max(
      origin.tileDistanceManhattan(destination),
      groundTiles(origin, destination))
  }

  @inline final def groundPixels(origin: Pixel, destination: Pixel): Double = {
    // Some maps used to have broken ground distance (due to BWTA,
    // which in particular suffered on maps with narrow ramps, eg. Plasma, Third World

    // Let's first check if we can use air distance. It's cheaper and more accurate.
    // We can "get away" with using air distance if we're in the same zone
    if (origin.zone == destination.zone) {
      return origin.pixelDistance(destination)
    }

    // This approximation -- calculating ground distance at tile resolution -- can potentially bite us.
    // Pun intended on "potentially" -- the risk here is using it for potential fields near a chokepoint
    // before which we're getting pixel-resolution distance and after which we're getting tile-resolution distance
    Math.max(
      origin.pixelDistance(destination),
      32.0 * groundTiles(origin.tile, destination.tile))
  }

  @inline final protected def groundTiles(origin: Tile, destination: Tile): Long = {
    if ( ! origin.valid) return impossiblyLargeDistance
    if ( ! destination.valid) return impossiblyLargeDistance
    ByOption
      .min(destination.zone.edges.view.map(edge =>
        edge.distanceGrid.getUnchecked(destination.i)
        + edge.distanceGrid.getUnchecked(origin.i).toLong))
      .getOrElse(impossiblyLargeDistance)
  }
}
