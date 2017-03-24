package Information.Geography

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
    val bases = CalculateBasePositions.calculate.map(townHallPosition => buildBase(townHallPosition, zones))
  
    bases.foreach(base => base.zone.bases += base)
    edges.foreach(edge => edge.zones.foreach(zone => zone.edges += edge))
    
    return zones
  }
  
  def buildZone(region:Region):Zone =
    new Zone(
      region,
      new TileRectangle(
        new Position(
          region.getPolygon.getPoints.asScala.map(_.getX).min,
          region.getPolygon.getPoints.asScala.map(_.getY).min).toTilePosition,
        new Position(
          region.getPolygon.getPoints.asScala.map(_.getX).max,
          region.getPolygon.getPoints.asScala.map(_.getY).max).toTilePosition),
      new ListBuffer[Base],
      new ListBuffer[ZoneEdge])
  
  def buildBase(townHallPosition:TilePosition, zones:Iterable[Zone]):Base = {
    val townHallArea = Protoss.Nexus.tileArea.add(townHallPosition)
    new Base(
      zones
        .find(_.contains(townHallPosition.toPosition))
        .getOrElse(zones.minBy(_.centroid.distancePixels(townHallPosition.pixelCenter))),
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
