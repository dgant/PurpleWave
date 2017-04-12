package Information.Geography.Calculations

import Mathematics.Shapes.Spiral
import Information.Geography.Types.{Base, Zone, ZoneEdge}
import ProxyBwapi.Races.Protoss
import Lifecycle.With
import Mathematics.Positions.TileRectangle
import Utilities.EnrichPosition._
import bwapi.{Position, TilePosition}
import bwta.{BWTA, Chokepoint, Region}

import scala.collection.JavaConverters._
import scala.collection.mutable
import scala.collection.mutable.ListBuffer

object ZoneBuilder {
  
  def build:Iterable[Zone] = {
    val zones = BWTA.getRegions.asScala.map(buildZone)
    val edges = BWTA.getChokepoints.asScala.map(choke => buildEdge(choke, zones))
    val bases = BaseFinder.calculate.map(townHallPosition => buildBase(townHallPosition, zones))
  
    bases.foreach(base => base.zone.bases += base)
    edges.foreach(edge => edge.zones.foreach(zone => zone.edges += edge))
    ensureAllTilesAreAssignedToAZone(zones)
    
    return zones
  }
  
  def ensureAllTilesAreAssignedToAZone(zones:Iterable[Zone]) = {
    With.geography.allTiles
      .filterNot(tile => zones.exists(_.contains(tile)))
      .foreach(tile => assignTile(tile, zones))
  }
  
  def assignTile(tile:TilePosition, zones:Iterable[Zone]) = {
    val groundHeight = With.game.getGroundHeight(tile)
    val candidates = zones.filter(_.groundHeight == groundHeight)
    val matchingZone = Spiral
      .points(5)
      .view
      .map(tile.add)
      .flatMap(neighborTile => candidates.view.filter(candidate => candidate.tiles.contains(neighborTile)).headOption)
      .headOption
    matchingZone
      .getOrElse(zones.minBy(_.centroid.pixelDistanceFast(tile.pixelCenter)))
      .tiles
      .add(tile)
  }
  
  def buildZone(region:Region):Zone = {
    val polygon = region.getPolygon
    val tileArea = new TileRectangle(
      new Position(
        polygon.getPoints.asScala.map(_.getX).min,
        polygon.getPoints.asScala.map(_.getY).min).tileIncluding,
      new Position(
        polygon.getPoints.asScala.map(_.getX).max,
        polygon.getPoints.asScala.map(_.getY).max).tileIncluding)
    val tiles = new mutable.HashSet[TilePosition]
    tiles ++= tileArea.tiles
      .filter(tile => {
        val tileRegion = BWTA.getRegion(tile)
        tileRegion != null && tileRegion.getCenter == region.getCenter
      })
      .toSet
    val groundHeight = tiles.headOption.map(With.game.getGroundHeight).getOrElse(1)
    new Zone(
      region,
      groundHeight,
      tileArea,
      tiles,
      new ListBuffer[Base],
      new ListBuffer[ZoneEdge])
  }
  
  def buildBase(townHallPosition:TilePosition, zones:Iterable[Zone]):Base = {
    val townHallArea = Protoss.Nexus.tileArea.add(townHallPosition)
    new Base(
      zones
        .find(_.contains(townHallPosition.pixelCenter))
        .getOrElse(zones.minBy(_.centroid.pixelDistanceFast(townHallPosition.pixelCenter))),
      townHallArea,
      With.game.getStartLocations.asScala.exists(_.tileDistance(townHallPosition) < 6))
  }
  
  def buildEdge(choke: Chokepoint, zones:Iterable[Zone]):ZoneEdge =
    new ZoneEdge(
      choke,
      Vector(
        choke.getRegions.first,
        choke.getRegions.second)
      .map(region => zones.find(_.centroid == region.getCenter).get))
}
