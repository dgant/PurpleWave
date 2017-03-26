package Information.Geography.Calculations

import Geometry.Shapes.Circle
import Geometry.{Clustering, TileRectangle}
import ProxyBwapi.Races.Protoss
import ProxyBwapi.UnitInfo.ForeignUnitInfo
import Startup.With
import Utilities.EnrichPosition._
import bwapi.TilePosition

import scala.collection.JavaConverters._
import scala.collection.mutable

object BaseFinder {
  
  def calculate:Set[TilePosition] = {
  
    //Find base positions
    val allHalls = clusteredResourcePatches.flatMap(bestTownHallTile).to[mutable.Set]
    
    //Remove conflicting/overlapping positions
    //O(n^3) but we only do it once
    
    allHalls.foreach(hall => {
      val conflictingHalls = List(hall) ++ allHalls.filter(otherHall => otherHall != hall && otherHall.distanceTile(hall) < 8)
      //Take the hall which is closest to a heyser
      val preferredHall = conflictingHalls
        .sortBy(With.game.getStartLocations.asScala.contains)
        .sortBy(hall => With.units.neutral
          .filter(_.unitClass.isGas)
          .map(_.tileDistance(hall))
          .min)
      conflictingHalls.filterNot(_ == preferredHall).foreach(allHalls.remove)
    })
    
    allHalls
  }
  
  private def clusteredResourcePatches:Iterable[Iterable[ForeignUnitInfo]] = {
    Clustering.group[ForeignUnitInfo](
      resourcePatches,
      32 * 12,
      true,
      (unit) => unit.pixelCenter).values
  }
  
  private def resourcePatches = {
    With.units.neutral.filter(unit => unit.unitClass.isGas || unit.unitClass.isMinerals && unit.initialResources > 0)
  }
  
  private def bestTownHallTile(resources:Iterable[ForeignUnitInfo]):Option[TilePosition] = {
    val centroid = resources.map(_.pixelCenter).centroid
    val centroidTile = centroid.toTilePosition
    val searchRadius = 10
    val candidates = Circle.points(searchRadius).map(centroidTile.add).filter(isLegalTownHallTile)
    if (candidates.isEmpty) return None
    Some(candidates.minBy(_.toPosition.add(64, 48).getDistance(centroid)))
  }
  
  private def isLegalTownHallTile(candidate:TilePosition):Boolean = {
    val buildingArea = Protoss.Nexus.tileArea.add(candidate)
    val exclusions =
      resourcePatches
        .map(resourcePatch => new TileRectangle(
          resourcePatch.tileTopLeft.subtract(3, 3),
          resourcePatch.tileTopLeft.add(3, 3).add(resourcePatch.unitClass.tileSize)))
    
    buildingArea.tiles.forall(With.game.isBuildable) &&
      ! resourcePatches.view.map(resourcePatch => resourcePatch.tileArea.expand(3, 3)).exists(buildingArea.intersects)
  }
}
