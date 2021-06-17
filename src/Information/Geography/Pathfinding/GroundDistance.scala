package Information.Geography.Pathfinding

import Mathematics.Maff
import Mathematics.Points.{Pixel, Tile}


trait GroundDistance {

  val impossiblyLargeDistanceTiles: Int = 256 * 256

  @inline final def groundPathExists(origin: Tile, destination: Tile): Boolean = {
    groundTilesManhattan(origin, destination) < impossiblyLargeDistanceTiles
  }

  @inline final def groundTilesManhattan(origin: Tile, destination: Tile): Int = {
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

  @inline final protected def groundTiles(origin: Tile, destination: Tile): Int = {
    if ( ! origin.valid) return impossiblyLargeDistanceTiles
    if ( ! destination.valid) return impossiblyLargeDistanceTiles
    Maff
      .min(destination.zone.edges.view.map(edge =>
          edge.distanceGrid.getUnchecked(destination)
        + edge.distanceGrid.getUnchecked(origin)))
      .getOrElse(impossiblyLargeDistanceTiles)
  }
}
