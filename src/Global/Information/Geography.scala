package Global.Information

import Geometry.{Clustering, Positions, TileRectangle}
import Startup.With
import Types.UnitInfo.{ForeignUnitInfo, FriendlyUnitInfo}
import Utilities.Caching.{Cache, CacheForever}
import Utilities.Enrichment.EnrichPosition._
import Utilities.Enrichment.EnrichUnitType._
import bwapi.{Position, TilePosition, UnitType}

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
    ourBaseHalls.view.map(_.tileTopLeft).headOption
      .getOrElse(With.units.ours.view.filter(_.utype.isBuilding).map(_.tileTopLeft).headOption
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
  val _resourceClusterCache = new CacheForever[Iterable[Iterable[ForeignUnitInfo]]](() => _getResourceClusters)
  val _resourceExclusionCache = new CacheForever[Iterable[TileRectangle]](() => _getExclusions)
  def _getResourceClusters:Iterable[Iterable[ForeignUnitInfo]] = {
    //Probably TODO: Exclude mineral blocks
    Clustering.group[ForeignUnitInfo](_getResources, 32 * 15, true, (unit) => unit.position).values
  }
  def _getExclusions:Iterable[TileRectangle] = {
    _getResources.map(_getExclusion)
  }
  def _getExclusion(unit:ForeignUnitInfo):TileRectangle = {
    new TileRectangle(unit.tileTopLeft.subtract(3, 3), unit.tileTopLeft.add(unit.utype.tileSize).add(3, 3))
  }
  def _getResources:Iterable[ForeignUnitInfo] = {
    With.units.neutral.filter(unit => unit.isMinerals || unit.isGas).filter(_.baseUnit.getInitialResources > 24)
  }
  def _calculateBasePositions:Iterable[TilePosition] = {
    _resourceClusterCache.get.map(_findBasePosition).filter(_.nonEmpty).map(_.get)
  }
  def _findBasePosition(resources:Iterable[ForeignUnitInfo]):Option[TilePosition] = {
    val centroid = resources.map(_.position).centroid
    val centroidTile = centroid.toTilePosition
    val searchRadius = 10
    val candidates =
      (-searchRadius to searchRadius).flatten(dx =>
        (-searchRadius to searchRadius).map(dy =>
          centroidTile.add(new TilePosition(dx, dy))))
    
    val extantCandidates = candidates.filter(_isLegalBasePosition)
    
    if (extantCandidates.isEmpty) return None
    
    Some(extantCandidates.minBy(_.toPosition.add(64, 48).getDistance(centroid)))
  }
  
  def _isLegalBasePosition(position:TilePosition):Boolean = {
    val exclusions = _resourceExclusionCache.get
    val buildingArea = new TileRectangle(position, position.add(UnitType.Zerg_Hatchery.tileSize))
    buildingArea.tiles.forall(With.game.isBuildable) && exclusions.forall( ! _.intersects(buildingArea))
  }
}
