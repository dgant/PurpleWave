package Processes

import Startup.With
import Types.EnemyUnitInfo
import bwapi.{TilePosition, UnitType}
import bwta.{BWTA, BaseLocation}

import scala.collection.JavaConverters._
import scala.collection.mutable

class Scout {
  
  val _knownEnemyUnits = new mutable.HashMap[Integer, EnemyUnitInfo].empty
  
  def onFrame() {
    //Stop tracking units that aren't enemies
    //Vespene geysers, add-ons, mind-controlled units, etc.
    With.enemyUnits
      .filter(_.exists) //Invisible units get .exists == false; so don't even try to get unit data if it's invisible
      .filter(unit => _knownEnemyUnits.contains(unit.getID))
      .filterNot(_isValidEnemyUnit)
      .map(_.getID)
      .foreach(_removeKnownUnit)
      
    //Track (reasonable) enemy units
    With.enemyUnits
      .filter(_isValidEnemyUnit)
      .filterNot(unit => _knownEnemyUnits.contains(unit.getID))
      .foreach(_addKnownUnit)
  }
  
  def _isValidEnemyUnit(unit:bwapi.Unit):Boolean = {
      unit.getPlayer.isEnemy(With.game.self) &&
      ! List(UnitType.None, UnitType.Unknown, UnitType.Zerg_Larva, UnitType.Zerg_Egg, UnitType.Resource_Vespene_Geyser).contains(unit.getType)
  }
  
  def onUnitDestroy(unit:bwapi.Unit) {
    _removeKnownUnit(unit.getID)
  }
  
  def knownEnemyUnits:Iterable[EnemyUnitInfo] = {
    _knownEnemyUnits.values
  }
  
  def _addKnownUnit(unit:bwapi.Unit) {
    val knownUnit =
      new EnemyUnitInfo(
        getID           = unit.getID,
        lastSeen        = With.game.getFrameCount,
        getPlayer       = unit.getPlayer,
        getPosition     = unit.getPosition,
        getTilePosition = unit.getTilePosition,
        getHitPoints    = unit.getHitPoints,
        getShields      = unit.getShields,
        getType         = unit.getType,
        isCompleted     = unit.isCompleted)
    _knownEnemyUnits.put(knownUnit.getID, knownUnit)
  }
  
  def _removeKnownUnit(id:Int) {
    _knownEnemyUnits.remove(id)
  }
  
  def unexploredStartLocations():Iterable[TilePosition] = {
    With.game.getStartLocations.asScala.filterNot(With.game.isExplored)
  }
  
  def mostBaselikeEnemyBuilding:Option[EnemyUnitInfo] = {
    knownEnemyUnits
      .toList
      .filterNot(unit => unit.getType.isBuilding)
      .sortBy(unit => unit.getType.isFlyer)
      .sortBy(unit => ! With.map.isTownHall(unit.getType))
      .headOption
  }
  
  def mostUnscoutedBases():Iterable[BaseLocation] = {
    BWTA.getBaseLocations.asScala
      .sortBy(base => ! base.isStartLocation)
      .sortBy(base => With.game.isExplored(base.getTilePosition))
  }
}
