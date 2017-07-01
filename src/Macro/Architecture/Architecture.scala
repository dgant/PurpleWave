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
  
  val exclusions      : mutable.ArrayBuffer[Exclusion]      = new mutable.ArrayBuffer[Exclusion]
  val unbuildable     : mutable.Set[Tile]                   = new mutable.HashSet[Tile]
  val unwalkable      : mutable.Set[Tile]                   = new mutable.HashSet[Tile]
  val ungassable      : mutable.Set[Tile]                   = new mutable.HashSet[Tile]
  val untownhallable  : mutable.Set[Tile]                   = new mutable.HashSet[Tile]
  val powered2Height  : mutable.Set[Tile]                   = new mutable.HashSet[Tile]
  val powered3Height  : mutable.Set[Tile]                   = new mutable.HashSet[Tile]
  val edgeWalkability : mutable.HashMap[ZoneEdge, TilePath] = new mutable.HashMap[ZoneEdge, TilePath]
  
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
    edgeWalkability .clear()
    recalculateExclusions()
    recalculatePower()
  }
  
  def buildable(tile: Tile): Boolean = {
    With.grids.buildable.get(tile) && ! unbuildable.contains(tile)
  }
  
  def walkable(tile: Tile): Boolean = {
    With.grids.walkable.get(tile) && ! unwalkable.contains(tile)
  }
  
  def affectsPathing(blockedArea: TileRectangle): Boolean = {
    blockedArea.tiles
      .flatMap(_.zone.edges)
      .toSet
      .exists(blocksPathing(_, blockedArea))
  }
  
  private def blocksPathing(edge: ZoneEdge, blockedArea: TileRectangle): Boolean = {
    lazy val start            = canaryTile(edge.zones.head)
    lazy val end              = canaryTile(edge.zones.last)
    lazy val maxTiles         = Math.max(20, 2 * start.groundPixels(end).toInt / 32)
    lazy val excludedBefore   = unwalkable.toSet
    lazy val excludedAfter    = unwalkable.toSet ++ blockedArea.tiles
    lazy val pathBefore       = PathFinder.manhattanGroundDistanceThroughObstacles(start, end, excludedBefore, maxTiles)
    lazy val pathAfter        = PathFinder.manhattanGroundDistanceThroughObstacles(start, end, excludedAfter,  maxTiles)
    edgeWalkability.put(edge, edgeWalkability.getOrElse(edge, pathBefore))
    pathBefore.tiles.isDefined && pathAfter.tiles.isEmpty
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
    
    if (With.visualization.enabled) {
      exclusions += new Exclusion(placement.buildingDescriptor.toString, margin)
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
