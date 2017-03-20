package Information.Geography

import Geometry.Shapes.Circle
import Geometry.{Clustering, TileRectangle}
import Performance.Caching.CacheForever
import ProxyBwapi.Races.Protoss
import ProxyBwapi.UnitInfo.ForeignUnitInfo
import Startup.With
import Utilities.TypeEnrichment.EnrichPosition._
import bwapi.TilePosition
class TownHallPositions {
  
  def townHallPositions:List[TilePosition] = townHallPositionCache.get
  
  private val townHallPositionCache = new CacheForever[List[TilePosition]](() => townHallPositionCalculate)
  
  private def getResourceClusters:Iterable[Iterable[ForeignUnitInfo]] = {
    Clustering.group[ForeignUnitInfo](getResources, 32 * 12, true, (unit) => unit.pixelCenter).values
  }
  
  private def getExclusions:Iterable[TileRectangle] = {
    getResources.map(getExclusion)
  }
  
  private def getExclusion(unit:ForeignUnitInfo):TileRectangle = {
    new TileRectangle(unit.tileTopLeft.subtract(3, 3), unit.tileTopLeft.add(unit.unitClass.tileSize).add(3, 3))
  }
  
  private def getResources:Iterable[ForeignUnitInfo] = {
    With.units.neutral.filter(unit => unit.isMinerals || unit.isGas).filter(_.initialResources > 0)
  }
  
  private def townHallPositionCalculate:List[TilePosition] = {
    getResourceClusters.flatMap(findBasePosition).toList
  }
  
  private def findBasePosition(resources:Iterable[ForeignUnitInfo]):Option[TilePosition] = {
    val centroid = resources.map(_.pixelCenter).centroid
    val centroidTile = centroid.toTilePosition
    val searchRadius = 10
    val candidates = Circle.points(searchRadius).map(centroidTile.add).filter(isLegalBasePosition)
    if (candidates.isEmpty) return None
    Some(candidates.minBy(_.toPosition.add(64, 48).getDistance(centroid)))
  }
  
  private def isLegalBasePosition(position:TilePosition):Boolean = {
    val exclusions = getExclusions
    val buildingArea = new TileRectangle(position, position.add(Protoss.Nexus.tileSize))
    buildingArea.tiles.forall(With.grids.buildableTerrain.get) && exclusions.forall( ! _.intersects(buildingArea))
  }
}
