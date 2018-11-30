package Information.Geography.Calculations

import Information.Geography.Types.{Base, Edge, Zone}
import Lifecycle.With
import Mathematics.Points.{Pixel, Tile, TileRectangle}
import Mathematics.Shapes.Spiral
import Utilities.ByOption
import bwta.{BWTA, Polygon, Region}

import scala.collection.JavaConverters._
import scala.collection.mutable
import scala.util.Random

object ZoneBuilder {
  
  def zones: Iterable[Zone] = {
    With.grids.walkableTerrain.initialize()
    val names   = Random.shuffle(PlaceNames.countries)
    val nameQ   = new mutable.Queue[String] ++ names
    var repeats = 1
    val zones = BWTA.getRegions.asScala.map(region => {
      if (nameQ.isEmpty) {
        repeats += 1
        names.map(name => name + " " + repeats).foreach(name => nameQ.enqueue(name))
      }
      buildZone(region, nameQ.dequeue)
    })
    mapObviousTilesToZones(zones)
    zones
  }
  
  def edges: Iterable[Edge] = BWTA.getChokepoints.asScala.map(new Edge(_))
  def bases: Iterable[Base] = {
    val bases   = BaseFinder.calculate.map(new Base(_))
    val names   = Random.shuffle(PlaceNames.cities)
    val nameQ   = new mutable.Queue[String] ++ names
    var repeats = 1
    bases.foreach(base => {
      if (nameQ.isEmpty) {
        repeats += 1
        names.map(name => name + " " + repeats).foreach(name => nameQ.enqueue(name))
      }
      base.name = nameQ.dequeue
    })
    bases
  }
  
  def mapObviousTilesToZones(zones: Iterable[Zone]) {
    //This will map most -- but not all tiles
    With.geography.allTiles
      .filterNot(tile => zones.exists(_.contains(tile)))
      .foreach(assignTile(_, zones))
  }
  
  def assignTile(tile: Tile, zones: Iterable[Zone]): Boolean = {
    val neighborZones = Spiral
      .points(5)
      .map(tile.add)
      .filter(_.valid)
      .flatMap(neighborTile => zones.view.find(candidate => candidate.tiles.contains(neighborTile)))
    val consensusZone = ByOption.maxBy(neighborZones.groupBy(x => x))(_._2.size).map(_._1)
    consensusZone
      .getOrElse(zones.minBy(_.centroid.tileDistanceSquared(tile)))
      .tiles
      .add(tile)
  }
  
  def buildZone(thisRegion: Region, name: String): Zone = {
    
    // The goal:        We want to check if two regions are the same.
    // The problem:     BWMirror gives its proxy objects no unique identifiers, hashcodes, or equality comparisons for objects.
    // The workaround:  Use properties of the polygon to construct a hopefully-unique identifier
    
    val thisIdentifier = new RegionIdentifier(thisRegion)
    val boundingBox = TileRectangle(
      Pixel(
        thisIdentifier.polygon.getPoints.asScala.map(_.getX).min,
        thisIdentifier.polygon.getPoints.asScala.map(_.getY).min)
        .tileIncluding,
      Pixel(
        32 + thisIdentifier.polygon.getPoints.asScala.map(_.getX).max,
        32 + thisIdentifier.polygon.getPoints.asScala.map(_.getY).max)
        .tileIncluding)
    val tiles = new mutable.HashSet[Tile]
    tiles ++= boundingBox.tiles
      .filter(tile => {
        val thatRegion = BWTA.getRegion(tile.bwapi)
        if (thatRegion == null) {
          false
        }
        else {
          val thatIdentifier = new RegionIdentifier(thatRegion)
          thatIdentifier.same(thisIdentifier)
        }
      })
      .toSet
    
    new Zone(name, thisRegion, boundingBox, tiles)
  }
  
  private class RegionIdentifier(region: Region) {
    val polygon    : Polygon = region.getPolygon
    val area       : Double  = polygon.getArea
    val perimeter  : Double  = polygon.getPerimeter
    
    def same(other: RegionIdentifier): Boolean = {
      area == other.area &&
      perimeter == other.perimeter
    }
  }
  
}
