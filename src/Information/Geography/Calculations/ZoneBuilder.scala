package Information.Geography.Calculations

import Geometry.TileRectangle
import Information.Geography.Types.{Base, Zone, ZoneEdge}
import ProxyBwapi.Races.Protoss
import Startup.With
import Utilities.EnrichPosition._
import bwapi.{Position, TilePosition}
import bwta.{BWTA, Chokepoint, Region}

import scala.collection.JavaConverters._
import scala.collection.mutable.ListBuffer

object ZoneBuilder {
  
  def build:Iterable[Zone] = {
    val zones = BWTA.getRegions.asScala.map(buildZone)
    val edges = BWTA.getChokepoints.asScala.map(choke => buildEdge(choke, zones))
    val bases = BaseFinder.calculate.map(townHallPosition => buildBase(townHallPosition, zones))
  
    bases.foreach(base => base.zone.bases += base)
    edges.foreach(edge => edge.zones.foreach(zone => zone.edges += edge))
    
    return zones
  }
  
  def buildZone(region:Region):Zone = {
    val polygon = region.getPolygon
    val tileArea = new TileRectangle(
      new Position(
        polygon.getPoints.asScala.map(_.getX).min,
        polygon.getPoints.asScala.map(_.getY).min).toTilePosition,
      new Position(
        polygon.getPoints.asScala.map(_.getX).max,
        polygon.getPoints.asScala.map(_.getY).max).toTilePosition)
    val tiles = tileArea.tiles
      .filter(tile => {
        val tileRegion = BWTA.getRegion(tile)
        tileRegion != null && tileRegion.getCenter == region.getCenter
      })
      .toSet
    new Zone(
      region,
      tileArea,
      tiles,
      new ListBuffer[Base],
      new ListBuffer[ZoneEdge])
  }
  
  def buildBase(townHallPosition:TilePosition, zones:Iterable[Zone]):Base = {
    val townHallArea = Protoss.Nexus.tileArea.add(townHallPosition)
    new Base(
      zones
        .find(_.contains(townHallPosition.toPosition))
        .getOrElse(zones.minBy(_.centroid.pixelDistance(townHallPosition.pixelCenter))),
      townHallArea,
      With.game.getStartLocations.asScala.exists(_.distanceTile(townHallPosition) < 6))
  }
  
  def buildEdge(choke: Chokepoint, zones:Iterable[Zone]):ZoneEdge =
    new ZoneEdge(
      choke,
      List(
        choke.getRegions.first,
        choke.getRegions.second)
      .map(region => zones.find(_.centroid == region.getCenter).get))
}
