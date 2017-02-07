package Processes

import Geometry.{Positions, Pylon, SpiralSearch, TileRectangle}
import Startup.With
import bwapi.{TilePosition, UnitType, WalkPosition}

import scala.collection.JavaConverters._
import scala.collection.mutable

class Architect {
  
  def getHq:TilePosition = {
    With.ourUnits
      .filter(_.getType.isBuilding)
      .sortBy(_.getType.isResourceDepot)
      .map(_.getTilePosition)
      .head
  }
  
  def placeBuilding(
    buildingType: UnitType,
    center:       TilePosition,
    margin:       Integer = 0,
    searchRadius: Integer = 20)
      :Option[TilePosition] = {
  
    SpiralSearch
      .forPointsInSpiral(center, searchRadius)
      .view
      .find(position => _canBuild(buildingType, position, margin))
  }
  
  // Try to place a collection of buildingshy
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
    
    SpiralSearch
      .forPointsInSpiral(center, searchRadius)
      .view
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
      val newExclusions = exclusions ++ List(new TileRectangle(
        searchPoint,
        new TilePosition(
          searchPoint.getX + nextBuilding.tileWidth - 1,
          searchPoint.getY + nextBuilding.tileHeight - 1)))
      
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
    position:           TilePosition,
    margin:             Integer                 = 0,
    exclusions:         Iterable[TileRectangle] = List.empty,
    hypotheticalPylon:  Option[TilePosition]    = None)
      :Boolean = {

    val marginArea = new TileRectangle(
      new TilePosition(
        position.getX - margin,
        position.getY - margin),
      new TilePosition(
        position.getX + margin + buildingType.tileWidth - 1,
        position.getY + margin + buildingType.tileHeight - 1))
    
    val buildingArea = new TileRectangle(
      position,
      new TilePosition(
        position.getX + buildingType.tileWidth - 1,
        position.getY + buildingType.tileHeight - 1))
    
    exclusions.filter(_.intersects(marginArea)).isEmpty &&
    //_rectangleContainsOnlyAWorker(marginArea) &&
    //_rectangleIsWalkable(marginArea) &&
    _rectangleIsBuildable(buildingArea, buildingType, hypotheticalPylon)
  }
  
  def _rectangleContainsOnlyAWorker(
    rectangle: TileRectangle)
      :Boolean = {
    val trespassingUnits = With.game.getUnitsInRectangle(rectangle.start.toPosition, rectangle.end.toPosition)
        .asScala
      .filterNot(_.isFlying)
  
    trespassingUnits.size <= 1 &&
      trespassingUnits.forall(_.getType.isWorker) &&
      trespassingUnits.forall(_.getPlayer == With.game.self)
  }
  
  def _rectangleIsWalkable(
    rectangle: TileRectangle)
      :Boolean = {
    val walkRectangle = Positions.toWalkRectangle(rectangle)
    (walkRectangle.start.getX to walkRectangle.end.getX).forall(x =>
      (walkRectangle.start.getY to walkRectangle.end.getY).forall(y =>
        _isWalkable(new WalkPosition(x, y))))
  }
  
  def _rectangleIsBuildable(
    area:              TileRectangle,
    buildingType:      UnitType,
    hypotheticalPylon: Option[TilePosition] = None)
      :Boolean = {
    
    (area.start.getX to area.end.getX).forall(x =>
      (area.start.getY to area.end.getY).forall(y => {
        val position = new TilePosition(x, y)
        _isBuildable(position) &&
          ((!buildingType.requiresPsi())   || With.game.hasPower(position) || hypotheticalPylon.exists(pylon => Pylon.powers(pylon, position))) &&
          ((!buildingType.requiresCreep()) || With.game.hasCreep(position))
      }))
  }
  
  val _walkableCache = new mutable.HashMap[WalkPosition, Boolean]
  def _isWalkable(walkPosition: WalkPosition):Boolean = {
    if ( ! _walkableCache.contains(walkPosition)) {
      _walkableCache.put(walkPosition, With.game.isWalkable(walkPosition))
    }
    _walkableCache(walkPosition)
  }
  
  val _buildableCache = new mutable.HashMap[TilePosition, Boolean]
  def _isBuildable(tilePosition: TilePosition):Boolean = {
    if ( ! _buildableCache.contains(tilePosition)) {
      _buildableCache.put(tilePosition, With.game.isBuildable(tilePosition))
    }
    _buildableCache(tilePosition)
  }
}
