package Information.Geography.Calculations

import Lifecycle.With
import Mathematics.Clustering
import Mathematics.Points.{Tile, TileRectangle}
import Mathematics.Shapes.Circle
import ProxyBwapi.Races.Protoss
import ProxyBwapi.UnitInfo.ForeignUnitInfo
import Utilities.EnrichPixel._

import scala.collection.JavaConverters._
import scala.collection.mutable

object BaseFinder {
  
  def calculate: Iterable[Tile] = {
    
    // Start locations are free base placements.
    val startTiles    = With.game.getStartLocations.asScala.map(new Tile(_)).toArray
    val startPixels   = startTiles.map(Protoss.Nexus.tileArea.add(_).midPixel)
  
    // Get every resource on the map
    val allResources  = With.units.neutral.filter(unit => unit.unitClass.isGas || unit.unitClass.isMinerals)
    
    // Get every resource that isn't a mineral block and isn't tied to a start location
    val baseResources = allResources.filterNot(_.isMineralBlocker)
    val expoResources = baseResources.filterNot(r => startPixels.exists(_.pixelDistanceFast(r.pixelCenter) <= With.configuration.baseRadiusPixels))
    
    // Cluster the expansion resources
    val clusters = clusterResourcePatches(expoResources)
  
    // Find base positions
    val exclusions = measureExclusions(expoResources)
    val expansions = clusters.flatMap(cluster => bestTownHallTile(cluster, exclusions))
    
    // Merge nearby base positions (like the middle of Heartbreak Ridge)
    mergeBases(startTiles ++ expansions)
  }
  
  private def clusterResourcePatches(resources: Iterable[ForeignUnitInfo]): Iterable[Iterable[ForeignUnitInfo]] = {
    Clustering.group[ForeignUnitInfo](
      resources,
      With.configuration.baseRadiusPixels,
      limitRegion = true,
      (unit) => unit.pixelCenter).values
  }
  
  private def measureExclusions(expansionResources: Iterable[ForeignUnitInfo]): Set[Tile] = {
    expansionResources.flatMap(_.tileArea.expand(3, 3).tiles).toSet
  }
  
  private def bestTownHallTile(
    resources   : Iterable[ForeignUnitInfo],
    exclusions  : Set[Tile])
      : Option[Tile] = {
  
    val centroid      = resources.map(_.pixelCenter).centroid.tileIncluding
    val altitude      = With.game.getGroundHeight(centroid.bwapi)
    val searchRadius  = 10
    val candidates =
      Circle
        .points(searchRadius)
        .map(centroid.add)
        .filter(tile => isLegalTownHallTile(tile, exclusions, altitude))
    
    if (candidates.isEmpty) return None
    Some(candidates.minBy(cost(_, resources)))
  }
  
  private def cost(tile: Tile, resources: Iterable[ForeignUnitInfo]): Double = {
    val corners = Protoss.Nexus.tileArea.add(tile).cornerPixels
    corners.map(corner =>
      resources.map(resource =>
        (2 *  resource.gasLeft +
              resource.mineralsLeft)
          * resource.pixelDistanceSquared(corner)).sum).min
  }
  
  private def isLegalTownHallTile(
    candidate   : Tile,
    exclusions  : Set[Tile],
    altitude    : Int)
      : Boolean = {
    
    if ( ! candidate.valid) return false
    val buildingArea = Protoss.Nexus.tileArea.add(candidate)
 
    buildingArea.tiles
      .forall(tile =>
        ! exclusions.contains(tile) &&
        With.game.isBuildable(tile.bwapi) &&
        With.game.getGroundHeight(tile.bwapi) == altitude)
  }
  
  private def mergeBases(bases: Iterable[Tile]): Iterable[Tile] = {
    
    val baseAreas = bases.map(Protoss.Nexus.tileArea.add)
    val basesLeft = new mutable.HashSet[TileRectangle] ++ baseAreas
    val output    = new mutable.ArrayBuffer[Tile]
  
    baseAreas.foreach(base =>
      if (basesLeft.contains(base)) {
        basesLeft.remove(base)
        output += base.startInclusive
    
        // Real lazy -- just remove all conflicting candidates (instead of, say, picking the best one or something)
        val conflicts = basesLeft.filter(_.midPixel.pixelDistanceFast(base.midPixel) < With.configuration.baseMergingRadiusPixels)
        basesLeft --= conflicts
    })
    
    output
  }
}
