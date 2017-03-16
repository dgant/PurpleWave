package Information.Geography

import Geometry.Shapes.Circle
import Geometry._
import Performance.Caching.{Cache, CacheForever, Limiter}
import ProxyBwapi.Races.Protoss
import ProxyBwapi.UnitInfo.{ForeignUnitInfo, UnitInfo}
import Startup.With
import Utilities.TypeEnrichment.EnrichPosition._
import Utilities.TypeEnrichment.EnrichUnitType._
import bwapi.TilePosition
import bwta.BWTA

import scala.collection.JavaConverters._
import scala.collection.mutable.ListBuffer

class Geography {
  
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
    val allBases = townHallPositions.map(townHallPosition => {
      val townHallArea = Protoss.Nexus.area.add(townHallPosition)
      new Base(
        zonesByRegionCenter.values
          .find(_.region.getPolygon.isInside(townHallPosition.toPosition))
          .getOrElse(zonesByRegionCenter.values.minBy(_.region.getCenter.pixelDistance(townHallPosition.centerPixel))),
        townHallArea,
        getHarvestingArea(townHallArea),
        With.game.getStartLocations.asScala.exists(_.tileDistance(townHallPosition) < 6))
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
      val townHall = With.units.buildings.filter(_.utype.isTownHall)
        .filter(townHall => base.zone.region.getPolygon.isInside(townHall.pixel))
        .toList
        .sortBy(_.distance(base.townHallArea.midpoint))
        .headOption
      base.townHall = townHall
      base.zone.owner = townHall.map(_.player).headOption.getOrElse(With.game.neutral)
    })
  
  def bases:Iterable[Base] = zones.flatten(_.bases)
  def ourBases:Iterable[Base] = bases.filter(_.zone.owner == With.self)
  def ourBaseHalls:Iterable[UnitInfo] = ourBases.filter(_.townHall.isDefined).map(_.townHall.get)
  def ourHarvestingAreas:Iterable[TileRectangle] = ourBases.map(_.harvestingArea)
  
  def home:TilePosition = homeCache.get
  private val homeCache = new Cache[TilePosition](5, () => homeCalculate)
  private def homeCalculate:TilePosition =
    ourBases.toList
      .sortBy( ! _.isStartLocation).map(_.centerTile)
      .headOption
      .getOrElse(With.units.ours.view.filter(_.utype.isBuilding).map(_.tileCenter).headOption
      .getOrElse(Positions.tileMiddle))
  
  def getHarvestingArea(townHallArea:TileRectangle):TileRectangle = {
    val resources = With.units
      .inRadius(townHallArea.midpoint.centerPixel, 32 * 10)
      .filter(_.isResource)
      .map(_.tileArea)
    
    (List(townHallArea) ++ resources).boundary
  }
  
  def townHallPositions:Iterable[TilePosition] = townHallPositionCache.get
  private val townHallPositionCache = new Cache[Iterable[TilePosition]](5, () => townHallPositionCalculate)
  private val resourceClusterCache = new CacheForever[Iterable[Iterable[ForeignUnitInfo]]](() => getResourceClusters)
  private val resourceExclusionCache = new CacheForever[Iterable[TileRectangle]](() => getExclusions)
  private def getResourceClusters:Iterable[Iterable[ForeignUnitInfo]] = Clustering.group[ForeignUnitInfo](getResources, 32 * 12, true, (unit) => unit.pixel).values
  private def getExclusions:Iterable[TileRectangle] = getResources.map(getExclusion)
  private def getExclusion(unit:ForeignUnitInfo):TileRectangle = new TileRectangle(unit.tileTopLeft.subtract(3, 3), unit.tileTopLeft.add(unit.utype.tileSize).add(3, 3))
  private def getResources:Iterable[ForeignUnitInfo] = With.units.neutral.filter(unit => unit.isMinerals || unit.isGas).filter(_.baseUnit.getInitialResources > 0)
  private def townHallPositionCalculate:Iterable[TilePosition] = resourceClusterCache.get.flatMap(findBasePosition)
  private def findBasePosition(resources:Iterable[ForeignUnitInfo]):Option[TilePosition] = {
    val centroid = resources.map(_.pixel).centroid
    val centroidTile = centroid.toTilePosition
    val searchRadius = 10
    val candidates = Circle.points(searchRadius).map(centroidTile.add).filter(isLegalBasePosition)
    if (candidates.isEmpty) return None
    Some(candidates.minBy(_.toPosition.add(64, 48).getDistance(centroid)))
  }
  private def isLegalBasePosition(position:TilePosition):Boolean = {
    val exclusions = resourceExclusionCache.get
    val buildingArea = new TileRectangle(position, position.add(Protoss.Nexus.tileSize))
    buildingArea.tiles.forall(With.grids.buildableTerrain.get) && exclusions.forall( ! _.intersects(buildingArea))
  }
}
