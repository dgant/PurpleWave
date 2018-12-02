package Information.Geography.Types

import Lifecycle.With
import Mathematics.Points.Pixel
import bwta.Chokepoint

import scala.collection.immutable.Seq

class Edge(choke: Chokepoint) {
  
  def otherSideof(zone: Zone): Zone =
    if (zones.head == zone)
      zones.last
    else
      zones.head // On Pathfinder there's an edge with the same zone on both sides. Grrr.
  
  lazy val pixelCenter  : Pixel = new Pixel(choke.getCenter)
  lazy val radiusPixels : Double = choke.getWidth / 2
  lazy val sidePixels   : Seq[Pixel] = Vector(new Pixel(choke.getSides.getLeft), new Pixel(choke.getSides.getRight))
  lazy val zones        : Vector[Zone] =
    Vector(
      choke.getRegions.getLeft,
      choke.getRegions.getRight)
    .map(region => With.geography.zones.minBy(
      _.centroid.pixelCenter.pixelDistanceSquared(
        new Pixel(region.getCenter))))
  
  def contains(pixel: Pixel): Boolean = pixelCenter.pixelDistance(pixel) <= radiusPixels
}