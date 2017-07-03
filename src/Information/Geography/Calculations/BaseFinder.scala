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
  
  
  private val baseToResourceRadius = 32.0 * 12.0
  
  def calculate: Iterable[Tile] = {
    
    
    // Ignore mineral blocks.
    // Calculate exclusions (assume mineral blocks blocking mineral patches are designed to be mined out).
    // Remove resources near start positions from consideration (to make sure we don't specify an in-base expansion.
    // Cluster the remaining resources.
    // For each cluster, identify a base
    
    // Start locations are free base placements.
    val startTiles    = With.game.getStartLocations.asScala.map(new Tile(_)).toArray
    val startPixels   = startTiles.map(Protoss.Nexus.tileArea.add(_).midPixel)
  
    // Get every resource on the map
    val allResources  = With.units.neutral.filter(unit => unit.unitClass.isGas || unit.unitClass.isMinerals)
    
    // Get every resource that isn't a mineral block and isn't tied to a start location
    val baseResources = allResources.filterNot(r => r.unitClass.isMinerals && r.initialResources <= With.configuration.blockerMineralThreshold)
    val expoResources = baseResources.filterNot(r => startPixels.exists(_.pixelDistanceFast(r.pixelCenter) <= baseToResourceRadius))
    
    // Cluster the expansion resources
    val clusters = clusterResourcePatches(expoResources)
  
    // Find base positions
    val exclusions = measureExclusions(expoResources)
    val expansions = clusters.flatMap(cluster => bestTownHallTile(cluster, exclusions))
    
    // Merge nearby base positions (like the middle of Heartbreak Ridge)
    mergeBases(expansions)
  }
  
  private def clusterResourcePatches(resources: Iterable[ForeignUnitInfo]): Iterable[Iterable[ForeignUnitInfo]] = {
    Clustering.group[ForeignUnitInfo](
      resources,
      32 * 12,
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
    val center = tile.topLeftPixel.add(Protoss.Nexus.width / 2, Protoss.Nexus.height / 2)
    resources.map(resource => 5 * resource.gasLeft * resource.pixelDistanceFast(center)).sum
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
    
    val candidates  = new mutable.HashSet[TileRectangle] ++ bases.map(Protoss.Nexus.tileArea.add)
    val output      = new mutable.ArrayBuffer[Tile]
    
    while (candidates.nonEmpty) {
      val candidate = candidates.head
      candidates.remove(candidate)
      output += candidate.startInclusive
  
      // Real lazy -- just remove all conflicting candidates (instead of, say, picking the best one or something)
      val conflicts = candidates.filter(_.intersects(candidate))
      candidates --= conflicts
    }
    
    output
  }
}
