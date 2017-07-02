package Information.Geography.Calculations

import Information.Geography.Types.{Base, Zone, Edge}
import Lifecycle.With
import Mathematics.Points.{Pixel, Tile, TileRectangle}
import Mathematics.Shapes.Spiral
import bwta.{BWTA, Region}

import scala.collection.JavaConverters._
import scala.collection.mutable

object ZoneBuilder {
  
  def zones: Iterable[Zone] = {
    val zones = BWTA.getRegions.asScala.map(buildZone)
    ensureAllTilesAreAssignedToAZone(zones)
    zones
  }
  
  def edges: Iterable[Edge] = BWTA.getChokepoints.asScala.map(new Edge(_))
  def bases: Iterable[Base]     = BaseFinder.calculate.map(new Base(_))
  
  
  def ensureAllTilesAreAssignedToAZone(zones: Iterable[Zone]) {
    With.geography.allTiles
      .filterNot(tile => zones.exists(_.contains(tile)))
      .foreach(tile => assignTile(tile, zones))
  }
  
  def assignTile(tile: Tile, zones: Iterable[Zone]): Boolean = {
    val groundHeight = With.game.getGroundHeight(tile.bwapi)
    val matchingZone = Spiral
      .points(5)
      .view
      .map(tile.add)
      .flatMap(neighborTile => zones.view.find(candidate => candidate.tiles.contains(neighborTile)))
      .headOption
    matchingZone
      .getOrElse(zones.minBy(_.centroid.tileDistanceSquared(tile)))
      .tiles
      .add(tile)
  }
  
  def buildZone(region: Region): Zone = {
    val polygon = region.getPolygon
    val tileArea = TileRectangle(
      Pixel(
        polygon.getPoints.asScala.map(_.getX).min,
        polygon.getPoints.asScala.map(_.getY).min)
        .tileIncluding,
      Pixel(
        polygon.getPoints.asScala.map(_.getX).max,
        polygon.getPoints.asScala.map(_.getY).max)
        .tileIncluding)
    val tiles = new mutable.HashSet[Tile]
    tiles ++= tileArea.tiles
      .filter(tile => {
        val tileRegion = BWTA.getRegion(tile.bwapi)
        tileRegion != null && tileRegion.getCenter == region.getCenter
      })
      .toSet
    
    new Zone(region, tileArea, tiles)
  }
}
