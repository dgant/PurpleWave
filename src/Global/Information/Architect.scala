package Global.Information

import Geometry.{Pylon, Spiral, TileRectangle}
import Startup.With
import Utilities.Enrichment.EnrichPosition._
import bwapi.{TilePosition, UnitType}

import scala.collection.JavaConverters._

class Architect {
  
  def placeBuilding(
    buildingType: UnitType,
    center:       TilePosition,
    margin:       Integer = 0,
    searchRadius: Integer = 20,
    exclusions:   Iterable[TileRectangle] = List.empty)
      :Option[TilePosition] = {
  
    Spiral
      .points(searchRadius)
      .view
      .map(center.add)
      .find(position => _canBuild(buildingType, position, margin, exclusions))
  }
  
  // Try to place a collection of buildings
  def placeBuildings(
    buildingTypes:      Iterable[UnitType],
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
      .map(searchPoint => _tryThing(searchPoint, buildingTypes, margin, searchRadius, exclusions, hypotheticalPylon))
      .find(_.isDefined)
      .getOrElse(None)
  }
  
  def _tryThing(
    searchPoint:        TilePosition,
    buildingTypes:      Iterable[UnitType],
    margin:             Integer                 = 0,
    searchRadius:       Integer                 = 20,
    exclusions:         Iterable[TileRectangle] = List.empty,
    hypotheticalPylon:  Option[TilePosition]    = None)
      :Option[Iterable[TilePosition]] = {
    
    val nextBuilding = buildingTypes.head
    
    if (_canBuild(nextBuilding, searchPoint, margin, exclusions, hypotheticalPylon)) {
      val newHypotheticalPylon = if (nextBuilding == UnitType.Protoss_Pylon) Some(searchPoint) else hypotheticalPylon
      val newExclusions = exclusions ++ List(new TileRectangle(searchPoint, searchPoint.add(nextBuilding.tileSize)))
      
      if (buildingTypes.size == 1) {
        return Some(List(searchPoint))
      }
      
      val rest = placeBuildings(
        buildingTypes.drop(1),
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
  
  def _canBuild(
    buildingType:       UnitType,
    tile:               TilePosition,
    margin:             Integer                 = 0,
    exclusions:         Iterable[TileRectangle] = List.empty,
    hypotheticalPylon:  Option[TilePosition]    = None)
      :Boolean = {
  
    val buildingArea = new TileRectangle(
      tile,
      tile.add(buildingType.tileSize))
    
    val marginArea = new TileRectangle(
      buildingArea.startInclusive.subtract(margin, margin),
      buildingArea.endExclusive.add(margin, margin))
    
    exclusions.filter(_.intersects(marginArea)).isEmpty &&
    _rectangleIsBuildable(buildingArea, buildingType, hypotheticalPylon) &&
    _rectangleContainsOnlyAWorker(buildingArea) &&
    marginArea.tiles.forall(With.grids.walkability.get)
  }
  
  def _rectangleContainsOnlyAWorker(rectangle: TileRectangle):Boolean = {
    val trespassingUnits =
      (rectangle.startInclusive.getX to rectangle.endExclusive.getX).flatten(x =>
        (rectangle.startInclusive.getY to rectangle.endExclusive.getY).flatten(y =>
          With.game.getUnitsOnTile(x, y).asScala
      ))
      .filterNot(_.isFlying)
  
    trespassingUnits.size <= 1 &&
      trespassingUnits.forall(_.getType.isWorker) &&
      trespassingUnits.forall(_.getPlayer == With.game.self)
  }
  
  def _rectangleIsBuildable(area: TileRectangle, buildingType: UnitType, hypotheticalPylon: Option[TilePosition] = None):Boolean = {
    area.tiles.forall(tile =>
      With.grids.buildability.get(tile) &&
        (( ! buildingType.requiresPsi())   || With.game.hasPower(tile) || hypotheticalPylon.exists(pylon => Pylon.powers(pylon, tile))) &&
        (( ! buildingType.requiresCreep()) || With.game.hasCreep(tile)))
  }
}
