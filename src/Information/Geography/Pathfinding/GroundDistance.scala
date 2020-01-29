package Information.Geography.Pathfinding

import Lifecycle.With
import Mathematics.Points.{Pixel, Tile}
import Utilities.ByOption

trait GroundDistance {

  val impossiblyLargeDistance: Long = 32L * 32L * 256L * 256L * 100L

  def groundPathExists(origin: Tile, destination: Tile): Boolean = {
    origin.zone == destination.zone || groundTiles(origin, destination) < impossiblyLargeDistance
  }

  def groundTilesManhattan(origin: Tile, destination: Tile): Long = {
    // Some maps have broken ground distance (due to continued reliance on BWTA,
    // which in particular seems to suffer on maps with narrow ramps, eg. Plasma, Third World
    // TODO: I think that issue has been fixed by using sub-buildtile walkability on those maps
    if (With.strategy.map.exists( ! _.trustGroundDistance)) {
      return origin.tileDistanceManhattan(destination)
    }

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

  def groundPixels(origin: Pixel, destination: Pixel): Double = {
    // Some maps have broken ground distance (due to continued reliance on BWTA,
    // which in particular seems to suffer on maps with narrow ramps, eg. Plasma, Third World
    if (With.strategy.map.exists( ! _.trustGroundDistance)) {
      return origin.pixelDistance(destination)
    }

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
      32 * groundTiles(origin.tileIncluding, destination.tileIncluding))
  }

  protected def groundTiles(origin: Tile, destination: Tile): Long = {
    ByOption
      .min(destination.zone.edges.map(edge =>
        edge.distanceGrid.get(destination) + edge.distanceGrid.get(origin).toLong))
      .getOrElse(impossiblyLargeDistance)
  }
}
