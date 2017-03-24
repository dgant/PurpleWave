package Information.Geography

import Information.Geography.Types.{Base, Zone, ZoneEdge}
import ProxyBwapi.Races.Protoss
import Startup.With
import Utilities.EnrichPosition._
import bwapi.TilePosition
import bwta.{BWTA, Chokepoint, Region}

import scala.collection.JavaConverters._
import scala.collection.mutable.ListBuffer

object BuildZones {
  
  def build:Iterable[Zone] = {
    
    val zones = BWTA.getRegions.asScala.map(buildZone)
    val bases = With.geography.townHallPositions.map(townHallPosition => buildBase(townHallPosition, zones))
    val edges = BWTA.getChokepoints.asScala.map(choke => buildEdge(choke, zones))
  
    bases.foreach(base => base.zone.bases += base)
    edges.foreach(edge => edge.zones.foreach(zone => zone.edges += edge))
    
    return zones
  }
  
  def buildZone(region:Region):Zone =
    new Zone(
      region.getCenter.toTilePosition,
      region,
      new ListBuffer[Base],
      new ListBuffer[ZoneEdge])
  
  def buildBase(townHallPosition:TilePosition, zones:Iterable[Zone]):Base = {
    val townHallArea = Protoss.Nexus.tileArea.add(townHallPosition)
    new Base(
      zones
        .find(_.region.getPolygon.isInside(townHallPosition.toPosition))
        .getOrElse(zones.minBy(_.region.getCenter.distancePixels(townHallPosition.pixelCenter))),
      townHallArea,
      With.geography.getHarvestingArea(townHallArea),
      With.game.getStartLocations.asScala.exists(_.distanceTile(townHallPosition) < 6))
  }
  
  def buildEdge(choke: Chokepoint, zones:Iterable[Zone]):ZoneEdge =
    new ZoneEdge(
      choke,
      List(
        choke.getRegions.first,
        choke.getRegions.second)
      .map(region => zones.find(_.region == region).get))
}
