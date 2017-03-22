package Information.Geography

import Information.Geography.Types.{Base, Zone, ZoneEdge}
import Performance.Caching.{CacheForever, Limiter}
import ProxyBwapi.Races.Protoss
import Startup.With
import bwta.BWTA
import Utilities.EnrichPosition._
import scala.collection.JavaConverters._
import scala.collection.mutable.ListBuffer

class Bases {
  
  def zones:Iterable[Zone] = {
    zoneUpdateLimiter.act()
    zoneCache.get
  }
  private val zoneCache = new CacheForever[Iterable[Zone]](() => zoneCalculate)
  private val zoneUpdateLimiter = new Limiter(10, updateZones)
  private def zoneCalculate:Iterable[Zone] = {
    //Build zones
    val zonesByRegionCenter = BWTA.getRegions.asScala.map(region =>
      (region.getCenter,
        new Zone(
          region.getCenter.toTilePosition,
          region,
          new ListBuffer[Base],
          new ListBuffer[ZoneEdge])))
      .toMap
    
    //Build bases
    val allBases = With.geography.townHallPositions.map(townHallPosition => {
      val townHallArea = Protoss.Nexus.tileArea.add(townHallPosition)
      new Base(
        zonesByRegionCenter.values
          .find(_.region.getPolygon.isInside(townHallPosition.toPosition))
          .getOrElse(zonesByRegionCenter.values.minBy(_.region.getCenter.distancePixels(townHallPosition.pixelCenter))),
        townHallArea,
        With.geography.getHarvestingArea(townHallArea),
        With.game.getStartLocations.asScala.exists(_.distanceTile(townHallPosition) < 6))
    })
    
    //Populate zones with bases
    allBases.foreach(base => base.zone.bases += base)
    
    //Add edges
    val edges = BWTA.getChokepoints.asScala.map(choke =>
      new ZoneEdge(
        choke,
        List(choke.getRegions.first, choke.getRegions.second)
          .map(region => zonesByRegionCenter(region.getCenter))))
    
    edges.foreach(edge => edge.zones.foreach(zone => zone.edges += edge))
    
    return zonesByRegionCenter.values
  }
  private def updateZones() =
    zoneCache.get.flatten(_.bases).foreach(base => {
      val townHall = With.units.buildings.filter(_.unitClass.isTownHall)
        .filter(townHall => base.zone.region.getPolygon.isInside(townHall.pixelCenter))
        .toList
        .sortBy(_.tileDistance(base.townHallArea.midpoint))
        .headOption
      base.townHall = townHall
      base.zone.owner = townHall.map(_.player).getOrElse(With.game.neutral)
    })
  
}
