package Global.Information

import Geometry.{Clustering, TileRectangle}
import Startup.With
import Types.UnitInfo.FriendlyUnitInfo
import Utilities.{Cache, CacheForever}
import bwapi.{Position, TilePosition, UnitType}
import Utilities.Enrichment.EnrichPosition._
import Utilities.Enrichment.EnrichUnitType._

import scala.collection.mutable
import scala.collection.JavaConverters._

class Geography {
  
  val isWalkable = new mutable.HashMap[TilePosition, Boolean] {
    override def default(tile: TilePosition): Boolean = {
      (0 to 4).forall(dx =>
        (0 to 4).forall(dy =>
          With.game.isWalkable(tile.getX * 8 + dx, tile.getY * 8 + dy)))}}
  
  def centerPosition:Position = {
    new Position(With.game.mapWidth * 32 / 2, With.game.mapHeight* 32 / 2)
  }
  
  def centerTilePosition:TilePosition = {
    new TilePosition(With.game.mapWidth / 2, With.game.mapHeight / 2)
  }
  def home:Position = {
    ourBaseHalls.view.map(_.position).headOption
      .getOrElse(With.units.ours.view.filter(_.unitType.isBuilding).map(_.position).headOption
      .getOrElse(new Position(0,0)))
  }
  
  def ourBaseHalls:Iterable[FriendlyUnitInfo] = {
    With.units.ours.filter(unit => unit.unitType.isTownHall && ! unit.flying)
  }
  
  def ourHarvestingAreas:Iterable[TileRectangle] = { _ourMiningAreasCache.get }
  val _ourMiningAreasCache = new Cache[Iterable[TileRectangle]] {
    duration = 24 * 15
    setCalculator(() => _recalculateOurMiningAreas)
  }
  def _recalculateOurMiningAreas:Iterable[TileRectangle] = {
    ourBaseHalls.map(base => {
      val nearbyUnits = With.units.inRadius(base.position, 32 * 10)
  
      val minerals = nearbyUnits
        .filter(_.unitType == UnitType.Resource_Mineral_Field)
        .map(_.position)
      
      val geysers = nearbyUnits
        .filter(unit => unit.unitType.isRefinery || unit.unitType == UnitType.Resource_Vespene_Geyser)
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
  val _basePositionsCache = new Cache[Iterable[TilePosition]] {
    duration = 24 * 20
    setCalculator(() => _calculateBasePositions)
  }
  val _resourceClusterCache = new CacheForever[Iterable[Iterable[TilePosition]]] {
    setCalculator(() => _getResourceClusters)
  }
  val _mineralExclusionCache = new CacheForever[Iterable[TileRectangle]] {
    setCalculator(() => _getMineralExclusions)
  }
  val _gasExclusionCache = new CacheForever[Iterable[TileRectangle]] {
    setCalculator(() => _getGasExclusions)
  }
  def _getMineralExclusions:Iterable[TileRectangle] = {
    val positions     = With.game.getStaticMinerals.asScala.map(_.getInitialTilePosition)
    val boundaryStart = new TilePosition(3, 3)
    val boundaryEnd   = new TilePosition(3 + UnitType.Resource_Mineral_Field.tileWidth, 3 + UnitType.Resource_Mineral_Field.tileHeight)
    positions.map(position => new TileRectangle(position.subtract(boundaryStart), position.add(boundaryEnd)))
  }
  def _getGasExclusions:Iterable[TileRectangle] = {
    val positions         = With.game.getStaticGeysers.asScala.map(_.getInitialTilePosition)
    val boundaryStart     = new TilePosition(3, 3)
    val boundaryEnd       = new TilePosition(3 + UnitType.Resource_Vespene_Geyser.tileWidth, 3 + UnitType.Resource_Vespene_Geyser.tileHeight)
    positions.map(position => new TileRectangle(position.subtract(boundaryStart), position.add(boundaryEnd)))
  }
  def _getResourceClusters:Iterable[Iterable[TilePosition]] = {
    val resources = With.game.getStaticMinerals.asScala.filter(unit => unit.getType.isGas || unit.getType.isMineralField).filter(unit => unit.getInitialResources > 50).map(_.getTilePosition)
    Clustering.group[TilePosition](resources, 32 * 18, (x) => x.toPosition).values
  }
  
  def _calculateBasePositions:Iterable[TilePosition] = {
    /*
    val occupiedPositions =
      (mineralPositions.flatten(mineral => UnitType.Resource_Mineral_Field.tiles.map(mineral.add(_))) ++
           gasPositions.flatten(gas     => UnitType.Resource_Vespene_Geyser.tiles.map(gas.add(_))))
    val illegalPositions = occupiedPositions.flatten(occupiedPosition =>
      (0 until 3).flatten(dx =>
        (0 until 3).map(dy =>
          occupiedPosition.add(new TilePosition(dx, dy)))))
      .toSet
      */
    
    val basePositions = _resourceClusterCache.get.map(_findBasePosition).filter(_.nonEmpty).map(_.get)
    basePositions
  }
  
  def _findBasePosition(positions:Iterable[TilePosition]):Option[TilePosition] = {
    val centroid = positions.centroid
    val searchRadius = 10
    val candidates =
      (-searchRadius to searchRadius).flatten(dx =>
        (-searchRadius to searchRadius).map(dy => {
          val basePosition = centroid.add(new TilePosition(dx, dy))
          val legal = _isLegalBasePosition(basePosition)
          if (legal) Some(basePosition) else None
        }))
    val extantCandidates = candidates.filter(_.nonEmpty).map(_.get)
    
    if (extantCandidates.isEmpty) return None
    Some(extantCandidates.minBy(candidate => positions.map(_.distanceSquared(candidate)).sum))
  }
  
  def _isLegalBasePosition(position:TilePosition):Boolean = {
    val exclusions = _mineralExclusionCache.get ++ _gasExclusionCache.get
    val buildingTiles = UnitType.Zerg_Hatchery.tiles
    buildingTiles.map(_.add(position)).forall(buildingTile =>
      With.game.isBuildable(buildingTile) &&
      exclusions.forall( ! _.contains(buildingTile)))
  }
}
