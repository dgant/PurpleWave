package Macro

import Lifecycle.With
import Mathematics.Points.{Tile, TileRectangle}
import Mathematics.Shapes.Spiral
import ProxyBwapi.Races.Protoss
import ProxyBwapi.UnitClass._

class Architect {
  
  def placeBuilding(
    buildingClass:  UnitClass,
    center:         Tile,
    margin:         Integer = 0,
    searchRadius:   Integer = 40,
    exclusions:     Iterable[TileRectangle] = Vector.empty)
      : Option[Tile] = {
  
    Spiral
      .points(searchRadius)
      .view
      .map(center.add)
      .find(position => canBuild(buildingClass, position, margin, exclusions))
  }
  
  def placeBuildings(
    buildingClasses:    Iterable[UnitClass],
    center:             Tile,
    margin:             Integer                 = 0,
    searchRadius:       Integer                 = 20,
    exclusions:         Iterable[TileRectangle] = Vector.empty,
    hypotheticalPylon:  Option[Tile]    = None)
      : Option[Iterable[Tile]] = {
    
    val exclusions:Vector[TileRectangle] = Vector.empty
    
    Spiral
      .points(searchRadius)
      .view
      .map(center.add)
      .map(searchPoint => tryBuilding(searchPoint, buildingClasses, margin, searchRadius, exclusions, hypotheticalPylon))
      .find(_.isDefined)
      .getOrElse(None)
  }
  
  private def tryBuilding(
    searchPoint:        Tile,
    buildingClasses:    Iterable[UnitClass],
    margin:             Integer                 = 0,
    searchRadius:       Integer                 = 20,
    exclusions:         Iterable[TileRectangle] = Vector.empty,
    hypotheticalPylon:  Option[Tile]            = None)
      : Option[Iterable[Tile]] = {
    
    val nextBuilding = buildingClasses.head
    
    if (canBuild(nextBuilding, searchPoint, margin, exclusions, hypotheticalPylon)) {
      val newHypotheticalPylon = if (nextBuilding == Protoss.Pylon) Some(searchPoint) else hypotheticalPylon
      val newExclusions = exclusions ++ Vector(new TileRectangle(searchPoint, searchPoint.add(nextBuilding.tileSize)))
      
      if (buildingClasses.size == 1) {
        return Some(Vector(searchPoint))
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
    tileTopleft:        Tile,
    margin:             Integer                 = 0,
    exclusions:         Iterable[TileRectangle] = Vector.empty,
    hypotheticalPylon:  Option[Tile]            = None)
      : Boolean = {
  
    lazy val buildingArea = buildingClass.tileArea.add(tileTopleft)
    lazy val marginArea   = buildingArea.expand(margin, margin)
  
    With.grids.buildable.get(tileTopleft) &&
    tileHasRequiredPsi(tileTopleft, buildingClass) &&
    marginArea.tiles.forall(tile => tile.valid && With.grids.walkable.get(tile)) &&
    rectangleIsBuildable(buildingArea, buildingClass, hypotheticalPylon) &&
    exclusions.forall( ! _.intersects(marginArea)) &&
    rectangleContainsOnlyAWorker(marginArea)
  }
  
  private def rectangleContainsOnlyAWorker(rectangle: TileRectangle):Boolean = {
    val trespassingUnits = With.units.inRectangle(rectangle).filterNot(_.flying)
    trespassingUnits.size <= 1 &&
      trespassingUnits.forall(_.unitClass.isWorker) &&
      trespassingUnits.forall(_.isOurs)
  }
  
  private def tileHasRequiredPsi(tileTopleft:Tile, buildingClass:UnitClass):Boolean = {
    ( ! buildingClass.requiresPsi) ||
    (buildingClass.tileWidth == 4 && With.grids.psi4x3.get(tileTopleft)) ||
    (buildingClass.tileWidth <  4 && With.grids.psi2x2and3x2.get(tileTopleft))
  }
  
  private def rectangleIsBuildable(
    area:               TileRectangle,
    buildingClass:      UnitClass,
    hypotheticalPylon:  Option[Tile] = None)
      : Boolean = {
    //HypotheticalPylon temporarily disabled
    area.tiles.forall(tile => With.grids.buildable.get(tile))
  }
}
