package Information.Geography.Types

import Information.Grids.Movement.GridGroundDistance
import Lifecycle.With
import Mathematics.Points.{Direction, Pixel, Tile}
import Mathematics.{Maff, Shapes}
import bwta.Chokepoint

final class Edge(choke: Chokepoint) {

  // On Pathfinder BWTA found an edge with the same zone on both sides
  def otherSideof(zone: Zone): Zone = zones.find(_ != zone).getOrElse(zones.head)
  lazy val pixelCenter  : Pixel = new Pixel(choke.getCenter)
  lazy val radiusPixels : Double = choke.getWidth / 2
  lazy val tiles        : Vector[Tile] = Shapes.Ray(sidePixels.head, sidePixels.last).toVector
  lazy val sidePixels   : Seq[Pixel] = Vector(new Pixel(choke.getSides.getLeft), new Pixel(choke.getSides.getRight))
  lazy val direction    : Direction = new Direction(sidePixels.head, sidePixels.last)
  lazy val endPixels    : Vector[Pixel] = Vector(-1, 1)
    .map(m => pixelCenter
      .radiateRadians(
        Maff.slowAtan2(
          sidePixels(0).y - sidePixels(1).y,
          sidePixels(0).x - sidePixels(1).x)
        + m * Math.PI / 2,
        32))
  lazy val zones: Vector[Zone] =
    Vector(
      choke.getRegions.getLeft,
      choke.getRegions.getRight)
    .map(region => With.geography.zones.minBy(
      _.centroid.center.pixelDistanceSquared(
        new Pixel(region.getCenter))))
  val distanceGrid: GridGroundDistance = new GridGroundDistance(tiles: _*)

  var lastPathfindId: Long = Long.MinValue

  private lazy val endsWalkable = endPixels.map(_.tile).forall(With.grids.walkableTerrain.get)
  def pixelTowards(zone: Zone): Pixel = endPixels.minBy(p => if (endsWalkable) zone.distanceGrid.get(p.tile) else p.pixelDistanceSquared(zone.centroid.center))
  def contains(pixel: Pixel): Boolean = pixelCenter.pixelDistance(pixel) <= radiusPixels
  def contains(tile: Tile): Boolean = contains(tile.center)
  def diameterPixels: Double = 2 * radiusPixels

  override def toString: String = f"Edge @ $pixelCenter (${radiusPixels.toInt}px)"
}