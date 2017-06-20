package Information.Geography.Pathfinding

import Lifecycle.With
import Mathematics.Points.Pixel

object PathFinder {
  
  def roughGroundDistance(from:Pixel, to:Pixel):Double = {
    
    val fromZone = from.zone
    val toZone = to.zone
    
    if (fromZone == toZone) {
      return from.pixelDistanceSlow(to)
    }
    
    if ( ! With.paths.exists(
      fromZone.centroid,
      toZone.centroid,
      requireBwta = true)) {
      return With.paths.impossiblyLargeDistance
    }
    
    val fromEdgeTiles = fromZone.edges.map(_.centerPixel.tileIncluding)
    val toEdgeTiles   =   toZone.edges.map(_.centerPixel.tileIncluding)
    
    fromEdgeTiles.map(fromEdgeTile =>
      toEdgeTiles.map(toEdgeTile =>
        from.pixelDistanceSlow(fromEdgeTile.pixelCenter) +
          to.pixelDistanceSlow(  toEdgeTile.pixelCenter) +
        With.paths.groundPixels(
          fromEdgeTile,
          toEdgeTile,
          requireBwta = true))
        .min)
      .min
  }
}
