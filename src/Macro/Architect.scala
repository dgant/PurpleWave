package Macro

import Mathematics.Shapes.Spiral
import ProxyBwapi.Races.Protoss
import ProxyBwapi.UnitClass._
import Lifecycle.With
import Mathematics.Positions.TileRectangle
import Utilities.EnrichPosition._
import bwapi.TilePosition

class Architect {
  
  def placeBuilding(
    buildingClass:  UnitClass,
    center:         TilePosition,
    margin:         Integer = 0,
    searchRadius:   Integer = 40,
    exclusions:     Iterable[TileRectangle] = List.empty)
      :Option[TilePosition] = {
  
    Spiral
      .points(searchRadius)
      .view
      .map(center.add)
      .find(position => canBuild(buildingClass, position, margin, exclusions))
  }
  
  def placeBuildings(
    buildingClasses:    Iterable[UnitClass],
    center:             TilePosition,
    margin:             Integer                 = 0,
    searchRadius:       Integer                 = 20,
    exclusions:         Iterable[TileRectangle] = List.empty,
    hypotheticalPylon:  Option[TilePosition]    = None)
      :Option[Iterable[TilePosition]] = {
    
    val exclusions:List[TileRectangle] = List.empty
    
    //For each point
    //  Try to find a place for the first building
    //    If first building successful
    //      Add that building's area to copy of exclusions
    //      Try to find places for the remaining buildings (with exclusions)
    //        If successful
    //          Prepend first building to locations and return
    //        If unsuccessful
    //          try the next point
    //    If first building unsuccessful
    //      try next point
    //  Having failed that
    //    return None
    
    Spiral
      .points(searchRadius)
      .view
      .map(center.add)
      .map(searchPoint => tryBuilding(searchPoint, buildingClasses, margin, searchRadius, exclusions, hypotheticalPylon))
      .find(_.isDefined)
      .getOrElse(None)
  }
  
  private def tryBuilding(
    searchPoint:        TilePosition,
    buildingClasses:    Iterable[UnitClass],
    margin:             Integer                 = 0,
    searchRadius:       Integer                 = 20,
    exclusions:         Iterable[TileRectangle] = List.empty,
    hypotheticalPylon:  Option[TilePosition]    = None)
      :Option[Iterable[TilePosition]] = {
    
    val nextBuilding = buildingClasses.head
    
    if (canBuild(nextBuilding, searchPoint, margin, exclusions, hypotheticalPylon)) {
      val newHypotheticalPylon = if (nextBuilding == Protoss.Pylon) Some(searchPoint) else hypotheticalPylon
      val newExclusions = exclusions ++ List(new TileRectangle(searchPoint, searchPoint.add(nextBuilding.tileSize)))
      
      if (buildingClasses.size == 1) {
        return Some(List(searchPoint))
      }
      
      val rest = placeBuildings(
        buildingClasses.drop(1),
        searchPoint,
        margin,
        searchRadius,
        newExclusions,
        newHypotheticalPylon)
    
      if (rest.isDefined) {
        return Some(searchPoint :: rest.get.toList)
      }
    }
  
    None
  }
  
  def canBuild(
    buildingClass:      UnitClass,
    tileTopleft:        TilePosition,
    margin:             Integer                 = 0,
    exclusions:         Iterable[TileRectangle] = List.empty,
    hypotheticalPylon:  Option[TilePosition]    = None)
      :Boolean = {
  
    val buildingArea = buildingClass.tileArea.add(tileTopleft)
    val marginArea = buildingArea.expand(margin, margin)
  
    tileHasRequiredPsi(tileTopleft, buildingClass) &&
    marginArea.tiles.forall(With.grids.walkable.get) &&
    rectangleIsBuildable(buildingArea, buildingClass, hypotheticalPylon) &&
    buildingArea.tiles.forall(_.valid) &&
    exclusions.forall( ! _.intersects(marginArea)) &&
    rectangleContainsOnlyAWorker(marginArea)
  }
  
  private def rectangleContainsOnlyAWorker(rectangle: TileRectangle):Boolean = {
    val trespassingUnits = With.units.inRectangle(rectangle).filterNot(_.flying)
    trespassingUnits.size <= 1 &&
      trespassingUnits.forall(_.unitClass.isWorker) &&
      trespassingUnits.forall(_.isOurs)
  }
  
  private def tileHasRequiredPsi(tileTopleft:TilePosition, buildingClass:UnitClass):Boolean = {
    ( ! buildingClass.requiresPsi) ||
    (buildingClass.tileWidth == 4 && With.grids.psi4x3.get(tileTopleft)) ||
    (buildingClass.tileWidth <  4 && With.grids.psi2x2and3x2.get(tileTopleft))
  }
  
  private def rectangleIsBuildable(
    area:               TileRectangle,
    buildingClass:      UnitClass,
    hypotheticalPylon:  Option[TilePosition] = None)
  :Boolean = {
    //HypotheticalPylon temporarily disabled
    area.tiles.forall(tile => With.grids.buildable.get(tile))
  }
}
