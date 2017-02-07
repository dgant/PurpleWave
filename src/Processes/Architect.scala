package Processes

import Geometry.{Positions, Pylon, SpiralSearch, TileRectangle}
import Startup.With
import bwapi.{TilePosition, UnitType, WalkPosition}

import scala.collection.JavaConverters._

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
    
    val nextBuilding = buildingTypes.head
    
    SpiralSearch
      .forPointsInSpiral(center, searchRadius)
      .find(point => {
        if (_canBuild(nextBuilding, point, margin, exclusions, hypotheticalPylon)) {
          val newHypotheticalPylon = if (nextBuilding == UnitType.Protoss_Pylon) Some(point) else hypotheticalPylon
          val newExclusions = exclusions :+ new TileRectangle(
            point,
            new TilePosition(
              point.getX + nextBuilding.width,
              point.getY + nextBuilding.height))
  
          val rest = placeBuildings(
            buildingTypes.drop(1),
            point,
            margin,
            searchRadius,
            newExclusions,
            newHypotheticalPylon)
          
          if (rest.isDefined) {
            val result = point :: rest.get.toList
            return Some(result)
          }
        }
        return None
      })
  
    return None
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
        position.getX + margin + buildingType.width,
        position.getY + margin + buildingType.height))
    
    val buildingArea = new TileRectangle(
      position,
      new TilePosition(
        position.getX + buildingType.width,
        position.getY + buildingType.height))
     
    _rectangleContainsOnlyAWorker(marginArea) &&
    _rectangleIsWalkable(marginArea) &&
    _rectangleIsBuildable(buildingArea, buildingType)
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
        With.game.isWalkable(new WalkPosition(x, y))))
  }
  
  def _rectangleIsBuildable(
    area:             TileRectangle,
    buildingType:      UnitType,
    hypotheticalPylon: Option[TilePosition] = None)
      :Boolean = {
    
    (area.start.getX to area.end.getX).forall(x =>
      (area.start.getY to area.end.getY).forall(y => {
        val position = new TilePosition(x, y)
        With.game.isBuildable(position) &&
          ((!buildingType.requiresPsi())   || With.game.hasPower(position) || hypotheticalPylon.exists(pylon => Pylon.powers(pylon, position))) &&
          ((!buildingType.requiresCreep()) || With.game.hasCreep(position))
      }))
  }
}
