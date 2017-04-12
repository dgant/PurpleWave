package Information.Geography.Calculations

import Mathematics.Shapes.Circle
import Mathematics.Clustering
import ProxyBwapi.Races.Protoss
import ProxyBwapi.UnitInfo.ForeignUnitInfo
import Lifecycle.With
import Mathematics.Positions.TileRectangle
import Utilities.EnrichPosition._
import bwapi.TilePosition

import scala.collection.JavaConverters._
import scala.collection.mutable

object BaseFinder {
  
  def calculate:Iterable[TilePosition] = {
  
    //Find base positions
    val allHalls = clusteredResourcePatches.flatMap(bestTownHallTile).to[mutable.Set]
    return removeConflictingBases(allHalls)
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
    val centroidTile = centroid.tileIncluding
    val searchRadius = 10
    val candidates =
      Circle
        .points(searchRadius)
        .map(centroidTile.add)
        .filter(isLegalTownHallTile)
    if (candidates.isEmpty) return None
    Some(candidates.minBy(_.topLeftPixel.add(64, 48).pixelDistanceSlow(centroid)))
  }
  
  private def isLegalTownHallTile(candidate:TilePosition):Boolean = {
    if ( ! candidate.valid) return false
    val buildingArea = Protoss.Nexus.tileArea.add(candidate)
    val exclusions =
      resourcePatches
        .map(resourcePatch => new TileRectangle(
          resourcePatch.tileTopLeft.subtract(3, 3),
          resourcePatch.tileTopLeft.add(3, 3).add(resourcePatch.unitClass.tileSize)))
    
    buildingArea.tiles.forall(With.game.isBuildable) &&
      ! resourcePatches.view.map(resourcePatch => resourcePatch.tileArea.expand(3, 3)).exists(buildingArea.intersects)
  }
  
  private def removeConflictingBases(halls:mutable.Set[TilePosition]):Iterable[TilePosition] = {
  
    val basesToRemove = new mutable.HashSet[TilePosition]
    
    //Remove conflicting/overlapping positions
    //O(n^3) but we only do it once
    halls.foreach(hall =>
      if ( ! basesToRemove.contains(hall)) {
        val conflictingHalls =
          List(hall) ++
          halls
            .filterNot(basesToRemove.contains)
            .filter(otherHall => otherHall != hall && otherHall.tileDistance(hall) < 8)
        
        //Take the hall which is closest to a geyser
        val preferredHall = conflictingHalls
          .sortBy(With.game.getStartLocations.asScala.contains)
          .sortBy(hall => With.units.neutral
            .filter(_.unitClass.isGas)
            .map(_.pixelDistanceFast(hall.pixelCenter))
            .min)
        basesToRemove -- conflictingHalls.filterNot(_ == preferredHall)
      })
    
    halls.filterNot(basesToRemove.contains)
  }
}
