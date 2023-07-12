package Information.Geography.Calculations

import Lifecycle.With
import Mathematics.Points.{Tile, TileRectangle}
import Mathematics.Shapes.Circle
import Mathematics.{Cluster, Maff}
import ProxyBwapi.Races.Protoss
import ProxyBwapi.UnitInfo.ForeignUnitInfo
import Strategery.Hunters

import scala.collection.JavaConverters._
import scala.collection.mutable

object FindBases {
  
  val baseRadiusPixels        : Double = 32.0 * 15.0
  val baseMergingRadiusPixels : Double = 32.0 * 12.0
  
  def apply(): Iterable[Tile] = {
    
    // Start locations are free base placements.
    val startTiles    = With.game.getStartLocations.asScala.map(new Tile(_)).toArray
    val startPixels   = startTiles.map(Protoss.Nexus.tileArea.add(_).center)
  
    // Get every resource on the map
    val allResources  = With.units.neutral.filter(unit => unit.unitClass.isGas || unit.unitClass.isMinerals)
    
    // Get every resource that isn't a mineral block and isn't tied to a start location
    val baseResources = allResources.filterNot(_.isBlocker)
    val expoResources = baseResources.filterNot(r => startPixels.exists(_.pixelDistance(r.pixel) <= baseRadiusPixels))
    
    // Cluster the expansion resources
    val clusters = clusterResourcePatches(expoResources)
  
    // Find base positions
    val exclusions = measureExclusions(expoResources)
    val expansions = clusters.flatMap(cluster => bestTownHallTile(cluster, exclusions))
    
    // Merge nearby base positions (like the middle of Heartbreak Ridge)
    val output = mergeBases(startTiles ++ expansions)

    if (output.size <= With.geography.startLocations.size) {
      With.logger.warn("Only found " + output.size + " bases (frame" + With.frame + ")")
      With.logger.warn("Total minerals visible: " + With.units.neutral.count(_.unitClass.isMinerals))
      With.logger.warn("Total gas visible: " + With.units.neutral.count(_.unitClass.isGas))
      With.logger.warn("Enemy units visible: " + With.units.enemy.size)
      With.logger.warn("Neutral units visible: ")
      With.units.neutral.view.map(_.toString).foreach(With.logger.warn)
      With.logger.warn("Static units known: " + With.game.getStaticNeutralUnits.size())
    }

    output
  }
  
  private def clusterResourcePatches(resources: Iterable[ForeignUnitInfo]): Iterable[Iterable[ForeignUnitInfo]] = {
    val shouldLimitRegion = ! Hunters() // Hack -- fix the top-right position on Hunters
    Cluster[ForeignUnitInfo](resources, baseRadiusPixels, limitRegion = shouldLimitRegion, _.pixel).values
  }
  
  private def measureExclusions(expansionResources: Iterable[ForeignUnitInfo]): Set[Tile] = {
    expansionResources.flatMap(_.tileArea.expand(3, 3).tiles).toSet
  }
  
  private def bestTownHallTile(
    resources   : Iterable[ForeignUnitInfo],
    exclusions  : Set[Tile])
      : Option[Tile] = {
  
    val centroid      = Maff.centroid(resources.map(_.pixel)).tile
    val altitude      = With.game.getGroundHeight(centroid.bwapi)
    val searchRadius  = 10
    val candidates =
      Circle(searchRadius)
        .map(centroid.add)
        .filter(isLegalTownHallTile(_, exclusions, altitude))
    
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
        val conflicts = basesLeft.filter(_.center.pixelDistance(base.center) < baseMergingRadiusPixels)
        basesLeft --= conflicts
    })
    
    output
  }
}
