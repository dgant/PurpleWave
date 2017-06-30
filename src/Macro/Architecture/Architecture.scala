package Macro.Architecture

import Information.Geography.Pathfinding.PathFinder
import Information.Geography.Types.{Zone, ZoneEdge}
import Lifecycle.With
import Mathematics.Points.{Tile, TileRectangle}
import Mathematics.Shapes.Spiral
import ProxyBwapi.Races.Protoss
import ProxyBwapi.UnitClass.{UnitClass, UnitClasses}

import scala.collection.mutable

class Architecture {
  
  val exclusions      : mutable.ArrayBuffer[Exclusion]                = new mutable.ArrayBuffer[Exclusion]
  val excludedTiles   : mutable.Set[Tile]                             = new mutable.HashSet[Tile]
  val powered2Height  : mutable.Set[Tile]                             = new mutable.HashSet[Tile]
  val powered3Height  : mutable.Set[Tile]                             = new mutable.HashSet[Tile]
  val edgeWalkability : mutable.HashMap[ZoneEdge, Boolean]            = new mutable.HashMap[ZoneEdge, Boolean]
  
  def usuallyNeedsMargin(unitClass: UnitClass): Boolean = {
    unitClass.isBuilding &&
    UnitClasses.all.exists(unit => ! unit.isFlyer && unit.whatBuilds._1 == unitClass) &&
    ! unitClass.isTownHall //Nexus margins bork FFEs. Down the road Hatcheries may need margins.
  }
  
  def reboot() {
    exclusions      .clear()
    excludedTiles   .clear()
    powered2Height  .clear()
    powered3Height  .clear()
    edgeWalkability .clear()
    recalculateExclusions()
    recalculatePower()
  }
  
  def walkable(tile: Tile): Boolean = {
    With.grids.walkable.get(tile) && ! excludedTiles.contains(tile)
  }
  
  def affectsPathing(blockedArea: TileRectangle): Boolean = {
    blockedArea.tiles
      .flatMap(_.zone.edges)
      .toSet
      .exists(affectsPathing(_, blockedArea))
  }
  
  def affectsPathing(edge: ZoneEdge, blockedArea: TileRectangle): Boolean = {
    lazy val start            = canaryTile(edge.zones.head)
    lazy val end              = canaryTile(edge.zones.last)
    lazy val maxTiles         = Math.max(20, 3 * start.groundPixels(end).toInt)
    lazy val excludedBefore   = excludedTiles.toSet
    lazy val excludedAfter    = excludedTiles.toSet ++ blockedArea.tiles
    lazy val walkableBefore   = PathFinder.manhattanGroundDistanceThroughObstacles(start, end, excludedBefore, maxTiles).isDefined
    lazy val walkableAfter    = PathFinder.manhattanGroundDistanceThroughObstacles(start, end, excludedAfter,  maxTiles).isDefined
    edgeWalkability.put(edge, edgeWalkability.getOrElse(edge, walkableBefore))
    walkableBefore && ! walkableAfter
  }
  
  /////////////
  // Margins //
  /////////////

  private def recalculateExclusions() {
    With.units.ours
      .filter(unit => usuallyNeedsMargin(unit.unitClass))
      .map(unit => Exclusion(
        "Margin for " + unit,
        unit.tileArea.expand(1, 1),
        gasAllowed      = true,
        townHallAllowed = true))
    .foreach(exclude)
    
    With.geography.bases
      .filterNot(_.owner.isEnemy)
      .map(base => Exclusion(
        "Harvesting area",
        base.harvestingArea,
        gasAllowed      = true,
        townHallAllowed = true))
    .foreach(exclude)
  }
  
  def assumePlacement(placement: Placement) {
    if (placement.tile.isEmpty) return
    addExclusion(placement)
    addPower(placement)
  }
  
  private def addExclusion(placement: Placement) {
    val margin = if (placement.buildingDescriptor.margin) 1 else 0
    exclusions += Exclusion(
      placement.buildingDescriptor.toString,
      TileRectangle(
        placement.tile.get.add(placement.buildingDescriptor.relativeMarginStart),
        placement.tile.get.add(placement.buildingDescriptor.relativeMarginEnd)),
      gasAllowed      = false,
      townHallAllowed = false)
  }
  
  private def exclude(exclusion: Exclusion) {
    exclusions    +=  exclusion
    excludedTiles ++= exclusion.areaExcluded.tiles
  }
  
  ///////////
  // Power //
  ///////////
  
  private def recalculatePower() {
    With.units.ours.filter(_.is(Protoss.Pylon)).map(_.tileTopLeft).foreach(addPower)
  }
  
  private def addPower(placement: Placement) {
    if (placement.buildingDescriptor.powers) {
      placement.tile.foreach(addPower)
    }
  }
  
  private def addPower(tile: Tile) {
    powered2Height  ++= With.grids.psi2Height.psiPoints.map(tile.add)
    powered3Height  ++= With.grids.psi3Height.psiPoints.map(tile.add)
  }
  
  /////////////////
  // Walkability //
  /////////////////
  
  private def canaryEdges: Set[ZoneEdge] = {
    With.geography.ourBases.flatMap(_.zone.edges).toSet
  }
  
  private def canaryTile(zone: Zone): Tile = {
    Spiral.points(20)
      .map(zone.centroid.add)
      .find(walkable)
      .getOrElse(zone.centroid)
  }
}
