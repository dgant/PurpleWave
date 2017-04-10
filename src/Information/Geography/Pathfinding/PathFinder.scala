package Information.Geography.Pathfinding

import Information.Geography.Types.Zone
import Lifecycle.With
import Utilities.EnrichPosition._
import bwapi.Position

object PathFinder {
  
  def roughGroundDistance(
    from:Position,
    to:Position):Double = {
    
    val fromZone = from.zone
    val toZone = to.zone
    
    if (fromZone == toZone) {
      return from.pixelDistanceSlow(to)
    }
    
    if ( ! With.paths.exists(
      fromZone.centroid.tileIncluding,
      toZone.centroid.tileIncluding,
      requireBwta = true)) {
      return With.paths.impossiblyLargeDistance
    }
    
    val fromEdgeTiles  = fromZone.edges.map(_.centerPixel.tileIncluding)
    val toEdgeTiles    =   toZone.edges.map(_.centerPixel.tileIncluding)
    
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
  
  //Doesn't work
  def roughGroundDistance2(
    from:Position,
    to:Position,
    explored:Set[Zone] = Set.empty):Double = {
    
    val zoneTo = to.zone
    val zoneContainingPixel = from.zone
    val zoneFrom:Zone =
      if (explored.contains(zoneContainingPixel))
        zoneContainingPixel
          .edges
          .find(_.centerPixel == from).get
          .zones
          .find(_ != zoneContainingPixel).get
      else
        zoneContainingPixel
    
    //If we're already here, return the straight-line distance
    if (zoneFrom == to.zone || to.zone.edges.exists(edge => edge.centerPixel.pixelDistanceSlow(to) <= edge.radiusPixels))
      return from.pixelDistanceSlow(to)
    
    val horizonEdges = zoneFrom.edges.filter(edge => edge.zones.exists(otherZone => otherZone != this && ! explored.contains(otherZone)))
    if (horizonEdges.isEmpty) return Double.PositiveInfinity
    
    return horizonEdges
      .map(edge =>
        from.pixelDistanceSlow(edge.centerPixel) +
          roughGroundDistance2(
          edge.centerPixel,
          to,
          explored ++ List(zoneFrom)))
      .min
  }
}
