package Information.Geography.Types

import Information.Grids.Movement.GridGroundDistance
import Lifecycle.With
import Mathematics.Points.{Pixel, PixelRay, Tile}
import Mathematics.PurpleMath
import bwta.Chokepoint

class Edge(choke: Chokepoint) {
  
  def otherSideof(zone: Zone): Zone =
    if (zones.head == zone)
      zones.last
    else
      zones.head // On Pathfinder there's an edge with the same zone on both sides. Grrr.
  
  lazy val pixelCenter  : Pixel = new Pixel(choke.getCenter)
  lazy val radiusPixels : Double = choke.getWidth / 2
  lazy val tiles        : Vector[Tile] = PixelRay(sidePixels.head, sidePixels.last).tilesIntersected.toVector
  lazy val sidePixels   : Seq[Pixel] = Vector(new Pixel(choke.getSides.getLeft), new Pixel(choke.getSides.getRight))
  lazy val endPixels: Vector[Pixel] = Vector(-1, 1)
    .map(m => pixelCenter
      .radiateRadians(
        PurpleMath.atan2(
          sidePixels(0).y - sidePixels(1).y,
          sidePixels(0).x - sidePixels(1).x)
        + m * Math.PI / 2,
        32))
  lazy val zones: Vector[Zone] =
    Vector(
      choke.getRegions.getLeft,
      choke.getRegions.getRight)
    .map(region => With.geography.zones.minBy(
      _.centroid.pixelCenter.pixelDistanceSquared(
        new Pixel(region.getCenter))))
  
  val distanceGrid: GridGroundDistance = new GridGroundDistance(tiles: _*)
  
  def contains(pixel: Pixel): Boolean = pixelCenter.pixelDistance(pixel) <= radiusPixels

  private lazy val endsWalkable = endPixels.map(_.tileIncluding).forall(With.grids.walkableTerrain.get)
  def pixelTowards(zone: Zone): Pixel = if (endsWalkable)
      endPixels.minBy(p => zone.distanceGrid.get(p.tileIncluding))
    else
      endPixels.minBy(_.pixelDistanceSquared(zone.centroid.pixelCenter))
}