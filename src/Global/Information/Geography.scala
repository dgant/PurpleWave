package Global.Information

import Geometry._
import Startup.With
import Types.UnitInfo.{ForeignUnitInfo, FriendlyUnitInfo}
import Utilities.Caching.{Cache, CacheForever}
import Utilities.Enrichment.EnrichPosition._
import Utilities.Enrichment.EnrichUnitType._
import bwapi.{Position, TilePosition, UnitType}
import bwta.BWTA

import scala.collection.JavaConverters._

class Geography {
  
  val _startPositionCache = new CacheForever[Iterable[TilePosition]](() => _startPositions)
  def startPositions = _startPositionCache.get
  def _startPositions:Iterable[TilePosition] = With.game.getStartLocations.asScala
  
  val _cacheChokes = new CacheForever[Iterable[Position]](() => BWTA.getChokepoints.asScala.map(_.getCenter))
  def chokes:Iterable[Position] = _cacheChokes.get
  def ourBases:Iterable[TilePosition] = ourBaseHalls.map(_.tileTopLeft)
  
  val _cacheBaseHalls = new Cache[Iterable[FriendlyUnitInfo]](1, () => With.units.ours.filter(unit => unit.utype.isTownHall && ! unit.flying))
  def ourBaseHalls:Iterable[FriendlyUnitInfo] = _cacheBaseHalls.get
  
  val _cacheHome = new Cache[TilePosition](24, () => _calculateHome)
  def home:TilePosition = _cacheHome.get
  def _calculateHome:TilePosition =
    ourBaseHalls.view.map(_.tileTopLeft).headOption
      .getOrElse(With.units.ours.view.filter(_.utype.isBuilding).map(_.tileTopLeft).headOption
        .getOrElse(Positions.tileMiddle))
  
  def ourHarvestingAreas:Iterable[TileRectangle] = _ourHarvestingAreaCache.get
  val _ourHarvestingAreaCache = new Cache[Iterable[TileRectangle]](24 * 5, () => _ourHarvestingAreas)
  def _ourHarvestingAreas:Iterable[TileRectangle] = {
    ourBaseHalls.filter(_.complete).map(base => {
      val nearbyUnits = With.units.inRadius(base.position, 32 * 10)
      val minerals = nearbyUnits
        .filter(_.utype == UnitType.Resource_Mineral_Field)
        .map(_.position)
      val geysers = nearbyUnits
        .filter(unit => unit.utype.isRefinery || unit.utype == UnitType.Resource_Vespene_Geyser)
        .flatten(unit => List(new Position(unit.left - 16, unit.top), new Position(unit.right + 16, unit.bottom)))
      val boxedUnits = minerals ++ geysers ++ Iterable(base.position)
      //Draw a box around the area
      val top    = boxedUnits.map(_.getY).min + 16
      val bottom = boxedUnits.map(_.getY).max + 16
      val left   = boxedUnits.map(_.getX).min + 16
      val right  = boxedUnits.map(_.getX).max + 16
      
      new TileRectangle(
        new TilePosition(left/32, top/32),
        new TilePosition(right/32, bottom/32))
    })
  }
  
  def basePositions:Iterable[TilePosition] = _basePositionsCache.get
  val _basePositionsCache = new Cache[Iterable[TilePosition]](24 * 60, () => _calculateBasePositions)
  val _resourceClusterCache = new CacheForever[Iterable[Iterable[ForeignUnitInfo]]](() => _getResourceClusters)
  val _resourceExclusionCache = new CacheForever[Iterable[TileRectangle]](() => _getExclusions)
  //Probably TODO: Exclude mineral blocks
  def _getResourceClusters:Iterable[Iterable[ForeignUnitInfo]] = Clustering.group[ForeignUnitInfo](_getResources, 32 * 15, true, (unit) => unit.position).values
  def _getExclusions:Iterable[TileRectangle] = _getResources.map(_getExclusion)
  def _getExclusion(unit:ForeignUnitInfo):TileRectangle = new TileRectangle(unit.tileTopLeft.subtract(3, 3), unit.tileTopLeft.add(unit.utype.tileSize).add(3, 3))
  def _getResources:Iterable[ForeignUnitInfo] = With.units.neutral.filter(unit => unit.isMinerals || unit.isGas).filter(_.baseUnit.getInitialResources > 24)
  def _calculateBasePositions:Iterable[TilePosition] = _resourceClusterCache.get.map(_findBasePosition).filter(_.nonEmpty).map(_.get)
  def _findBasePosition(resources:Iterable[ForeignUnitInfo]):Option[TilePosition] = {
    val centroid = resources.map(_.position).centroid
    val centroidTile = centroid.toTilePosition
    val searchRadius = 10
    val candidates = Circle.points(searchRadius).map(centroidTile.add).filter(_isLegalBasePosition)
    if (candidates.isEmpty) return None
    Some(candidates.minBy(_.toPosition.add(64, 48).getDistance(centroid)))
  }
  
  def _isLegalBasePosition(position:TilePosition):Boolean = {
    val exclusions = _resourceExclusionCache.get
    val buildingArea = new TileRectangle(position, position.add(UnitType.Zerg_Hatchery.tileSize))
    buildingArea.tiles.forall(With.game.isBuildable) && exclusions.forall( ! _.intersects(buildingArea))
  }
}
