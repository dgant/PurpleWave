package Macro.Architecture

import Information.Geography.Pathfinding.PathFinder
import Information.Geography.Pathfinding.PathFinder.TilePath
import Information.Geography.Types.{Zone, ZoneEdge}
import Lifecycle.With
import Mathematics.Points.{Tile, TileRectangle}
import Mathematics.Shapes.Spiral
import ProxyBwapi.Races.Protoss
import ProxyBwapi.UnitClass.{UnitClass, UnitClasses}
import ProxyBwapi.UnitInfo.UnitInfo

import scala.collection.mutable

class Architecture {
  
  val exclusions      : mutable.ArrayBuffer[Exclusion]            = new mutable.ArrayBuffer[Exclusion]
  val unbuildable     : mutable.Set[Tile]                         = new mutable.HashSet[Tile]
  val unwalkable      : mutable.Set[Tile]                         = new mutable.HashSet[Tile]
  val ungassable      : mutable.Set[Tile]                         = new mutable.HashSet[Tile]
  val untownhallable  : mutable.Set[Tile]                         = new mutable.HashSet[Tile]
  val powered2Height  : mutable.Set[Tile]                         = new mutable.HashSet[Tile]
  val powered3Height  : mutable.Set[Tile]                         = new mutable.HashSet[Tile]
  val existingPaths   : mutable.HashMap[ZoneEdge, TilePathCache]  = new mutable.HashMap[ZoneEdge, TilePathCache]
  
  class TilePathCache {
    var path  : Option[TilePath]  = None
    var tiles : Set[Tile]         = Set.empty
    var valid : Boolean           = false
    
    def set(newPath: TilePath) {
      path  = Some(newPath)
      tiles = newPath.tiles.map(_.toSet).getOrElse(Set.empty)
      valid = true
    }
  
    def update() {
      valid = valid && path.get.tiles.get.forall(With.architecture.walkable)
    }
  }
    
  def usuallyNeedsMargin(unitClass: UnitClass): Boolean = {
    unitClass.isBuilding &&
    UnitClasses.all.exists(unit => ! unit.isFlyer && unit.whatBuilds._1 == unitClass) &&
    ! unitClass.isTownHall //Nexus margins bork FFEs. Down the road Hatcheries may need margins.
  }
  
  def reboot() {
    exclusions      .clear()
    unbuildable     .clear()
    unwalkable      .clear()
    ungassable      .clear()
    untownhallable  .clear()
    powered2Height  .clear()
    powered3Height  .clear()
    recalculateExclusions()
    recalculatePower()
    updatePaths()
  }
  
  private def updatePaths() {
    existingPaths.values.foreach(_.update())
  }
  
  def buildable(tile: Tile): Boolean = {
    With.grids.buildable.get(tile) && ! unbuildable.contains(tile)
  }
  
  def walkable(tile: Tile): Boolean = {
    With.grids.walkable.get(tile) && ! unwalkable.contains(tile)
  }
  
  def breaksPathing(blockedArea: TileRectangle): Boolean = {
    // This is a critical but potentially expensive check.
    //
    // Cost of not checking:            Walling ourself in and losing the game because of a dumb Pylon
    // Cost of checking inefficiently:  Dropping frames and getting disqualified because of fear of dumb Pylons
    //
    // So let's check this every time, but really focus on making it an inexpensive check
    
    // If we have a margin, then it's not possible to break pathing.
    if (blockedArea.tilesSurrounding.forall(tile => ! tile.valid || walkable(tile))) {
      return false
    }
    
    blockedArea.tiles
      .flatMap(_.zone.edges)
      .toSet
      .exists(blocksPathing(_, blockedArea))
  }
  
  private def blocksPathing(edge: ZoneEdge, blockedArea: TileRectangle): Boolean = {
    
    if ( ! existingPaths.contains(edge)) {
      existingPaths.put(edge, new TilePathCache)
    }
    
    lazy val start                  = canaryTile(edge.zones.head)
    lazy val end                    = canaryTile(edge.zones.last)
    lazy val maxTiles               = Math.max(20, 5 * start.groundPixels(end).toInt / 32)
    lazy val blockedTiles           = blockedArea.tiles
    lazy val excludedBefore         = unwalkable.toSet
    lazy val excludedAfter          = unwalkable.toSet ++ blockedTiles
    lazy val pathBefore             = PathFinder.manhattanGroundDistanceThroughObstacles(start, end, excludedBefore, maxTiles)
    lazy val pathAfter              = PathFinder.manhattanGroundDistanceThroughObstacles(start, end, excludedAfter,  maxTiles)
    
    // Cache the before-path, if we haven't already
    if ( ! existingPaths(edge).valid) {
      existingPaths(edge).set(pathBefore)
    }
    
    // If we're not even touching the original path, then everything is fine.
    if ( ! blockedTiles.exists(existingPaths(edge).tiles.contains)) {
      return false
    }
    
    // If the path was already blocked, we're screwed anyway, so whatever.
    if ( ! pathBefore.pathExists) {
      return false
    }
    
    // Fine, we'll do some very expensive pathfinding :(
    // TODO: We could pathfind less by only doing testing the #1 candidate (and rejecting it if it fails the pathfinding test)
    
    // If our new path is successful, let's use that one instead
    if (pathAfter.pathExists) {
      existingPaths(edge).set(pathAfter)
    }
    
    pathAfter.pathExists
  }
  
  def assumePlacement(placement: Placement) {
    if (placement.tile.isEmpty) return
    
    val tile = placement.tile.get
  
    val area = TileRectangle(
      tile.add(placement.buildingDescriptor.relativeBuildStart),
      tile.add(placement.buildingDescriptor.relativeBuildEnd))
    
    val margin = TileRectangle(
      tile.add(placement.buildingDescriptor.relativeMarginStart),
      tile.add(placement.buildingDescriptor.relativeMarginEnd))
    
    unbuildable     ++= margin.tiles
    unwalkable      ++= area.tiles
    untownhallable  ++= area.tiles
    ungassable      ++= area.tiles
  
    if (placement.buildingDescriptor.powers) {
      addPower(tile)
    }
  
    updatePaths() //Invalidate any paths which no longer work
    
    if (With.visualization.enabled) {
      exclusions += Exclusion(placement.buildingDescriptor.toString, margin)
    }
  }
  
  /////////////
  // Margins //
  /////////////
  
  private def recalculateUnwalkable() {
    unwalkable ++= With.units.all
      .filter(unit => unit.unitClass.isBuilding && ! unit.flying)
      .flatMap(_.tileArea.tiles)
  }
  
  private def recalculateUnbuildable() {
    unbuildable ++= With.units.ours
      .filter(unit => unit.unitClass.isBuilding && ! unit.flying)
      .flatMap(unit =>
        if (usuallyNeedsMargin(unit.unitClass))
          unit.tileArea.expand(1, 1).tiles
        else
          unit.tileArea.tiles)
  }

  private def recalculateExclusions() {
    val forUnbuildable  = With.units.all.filter(isGroundBuilding)
    val forUnwalkable   = With.units.ours.filter(unit => isGroundBuilding(unit) && usuallyNeedsMargin(unit.unitClass))
    val harvestingAreas = With.geography.bases.map(_.harvestingArea)
    
    unbuildable     ++= forUnbuildable.flatMap(_.tileArea.tiles)
    unwalkable      ++= unbuildable
    unwalkable      ++= forUnwalkable.flatMap(_.tileArea.expand(1, 1).tiles)
    untownhallable  ++= unbuildable
    unbuildable     ++= harvestingAreas.flatMap(_.tiles)
      
    if (With.visualization.enabled) {
      exclusions ++= harvestingAreas.map(area => Exclusion("Harvesting area", area))
      exclusions ++= forUnwalkable.map(unit => Exclusion("Margin for " + unit, unit.tileArea.expand(1, 1)))
    }
  }
  
  private def isGroundBuilding(unit: UnitInfo): Boolean = {
    ! unit.flying && unit.unitClass.isBuilding
  }
  
  ///////////
  // Power //
  ///////////
  
  private def recalculatePower() {
    With.units.ours.filter(_.is(Protoss.Pylon)).map(_.tileTopLeft).foreach(addPower)
  }
  
  private def addPower(tile: Tile) {
    powered2Height  ++= With.grids.psi2Height.psiPoints.map(tile.add)
    powered3Height  ++= With.grids.psi3Height.psiPoints.map(tile.add)
  }
  
  /////////////////
  // Walkability //
  /////////////////
  private def canaryTile(zone: Zone): Tile = {
    Spiral.points(20)
      .map(zone.centroid.add)
      .find(walkable)
      .getOrElse(zone.centroid)
  }
}
