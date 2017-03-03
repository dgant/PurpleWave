package Global.Information

import Geometry.{Clustering, Positions, TileRectangle}
import Startup.With
import Types.UnitInfo.FriendlyUnitInfo
import Utilities.Caching.{Cache, CacheForever}
import Utilities.Enrichment.EnrichPosition._
import Utilities.Enrichment.EnrichUnitType._
import bwapi.{Position, TilePosition, UnitType}

import scala.collection.JavaConverters._

class Geography {
  
  def centerPosition:Position = {
    new Position(With.game.mapWidth * 32 / 2, With.game.mapHeight* 32 / 2)
  }
  
  def centerTilePosition:TilePosition = {
    new TilePosition(With.game.mapWidth / 2, With.game.mapHeight / 2)
  }
  
  val _cacheHome = new Cache[TilePosition](24, () => _calculateHome)
  def home:TilePosition = {
    _cacheHome.get
  }
  def _calculateHome:TilePosition = {
    ourBaseHalls.view.map(_.tilePosition).headOption
      .getOrElse(With.units.ours.view.filter(_.utype.isBuilding).map(_.tilePosition).headOption
        .getOrElse(Positions.tileMiddle))
  }
  
  def ourBaseHalls:Iterable[FriendlyUnitInfo] = {
    With.units.ours.filter(unit => unit.utype.isTownHall && ! unit.flying)
  }
  
  def ourHarvestingAreas:Iterable[TileRectangle] = { _ourHarvestingAreaCache.get }
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
  val _resourceClusterCache = new CacheForever[Iterable[Iterable[TilePosition]]](() => _getResourceClusters)
  val _mineralExclusionCache = new CacheForever[Iterable[TileRectangle]](() => _getMineralExclusions)
  val _gasExclusionCache = new CacheForever[Iterable[TileRectangle]](() => _getGasExclusions)
  def _getMineralExclusions:Iterable[TileRectangle] = {
    val positions     = With.game.getStaticMinerals.asScala.map(_.getInitialTilePosition)
    val boundaryStart = new TilePosition(3, 3)
    val boundaryEnd   = boundaryStart.add(UnitType.Resource_Mineral_Field.tileWidth - 1, UnitType.Resource_Mineral_Field.tileHeight - 1)
    positions.map(position => new TileRectangle(position.subtract(boundaryStart), position.add(boundaryEnd)))
  }
  def _getGasExclusions:Iterable[TileRectangle] = {
    val positions         = With.game.getStaticGeysers.asScala.map(_.getInitialTilePosition)
    val boundaryStart     = new TilePosition(3, 3)
    val boundaryEnd       = boundaryStart.add(UnitType.Resource_Vespene_Geyser.tileWidth - 1, UnitType.Resource_Vespene_Geyser.tileHeight - 1)
    positions.map(position => new TileRectangle(position.subtract(boundaryStart), position.add(boundaryEnd)))
  }
  def _getResourceClusters:Iterable[Iterable[TilePosition]] = {
    val resources = (With.game.getStaticMinerals.asScala ++ With.game.getStaticGeysers.asScala).filter(unit => unit.getInitialResources > 50).map(_.getTilePosition)
    Clustering.group[TilePosition](resources, 32 * 15, true, (x) => x.toPosition).values
  }
  
  def _calculateBasePositions:Iterable[TilePosition] = {
    val basePositions = _resourceClusterCache.get.map(_findBasePosition).filter(_.nonEmpty).map(_.get)
    basePositions
  }
  
  def _findBasePosition(resources:Iterable[TilePosition]):Option[TilePosition] = {
    val centroid = resources.centroid
    val searchRadius = 10
    val candidates =
      (-searchRadius to searchRadius).flatten(dx =>
        (-searchRadius to searchRadius).map(dy => {
          val basePosition = centroid.add(new TilePosition(dx, dy))
          val legal = _isLegalBasePosition(basePosition, centroid)
          if (legal) Some(basePosition) else None
        }))
    val extantCandidates = candidates.filter(_.nonEmpty).map(_.get)
    
    if (extantCandidates.isEmpty) return None
    Some(extantCandidates.minBy(candidate => resources.map(_.distanceSquared(candidate)).max))
  }
  
  def _isLegalBasePosition(position:TilePosition, centroid:TilePosition):Boolean = {
    val exclusions = _mineralExclusionCache.get ++ _gasExclusionCache.get
    val buildingTiles = UnitType.Zerg_Hatchery.tiles
    buildingTiles.map(_.add(position)).forall(buildingTile =>
      With.game.isBuildable(buildingTile) &&
      With.game.getGroundHeight(buildingTile) == With.game.getGroundHeight(centroid) &&
      exclusions.forall( ! _.contains(buildingTile)))
  }
}
