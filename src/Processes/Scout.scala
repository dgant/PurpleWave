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
      .filterNot(_isValidEnemyUnit)
      .map(_.getID)
      .foreach(_knownEnemyUnits.remove(_))
      
    //Track (reasonable) enemy units
    With.enemyUnits
      .filter(_isValidEnemyUnit)
      .filterNot(unit => _knownEnemyUnits.contains(unit.getID))
      .filterNot(unit => List(UnitType.None, UnitType.Unknown,UnitType.Zerg_Larva, UnitType.Zerg_Egg).contains(unit.getType))
      .map(unit => new EnemyUnitInfo(
        getID           = unit.getID,
        lastSeen        = With.game.getFrameCount,
        getPlayer       = unit.getPlayer,
        getPosition     = unit.getPosition,
        getTilePosition = unit.getTilePosition,
        getHitPoints    = unit.getHitPoints,
        getShields      = unit.getShields,
        getType         = unit.getType,
        isCompleted     = unit.isCompleted))
      .foreach(knownUnit => _knownEnemyUnits.put(knownUnit.getID, knownUnit))
  }
  
  def _isValidEnemyUnit(unit:bwapi.Unit):Boolean = {
    unit.exists &&
      unit.getPlayer.isEnemy(With.game.self) &&
      ! List(UnitType.None, UnitType.Unknown,UnitType.Zerg_Larva, UnitType.Zerg_Egg).contains(unit.getType)
  }
  
  def onUnitDestroy(unit:bwapi.Unit) {
    _knownEnemyUnits.remove(unit.getID)
  }
  
  def knownEnemyUnits:Iterable[EnemyUnitInfo] = {
    _knownEnemyUnits.values
  }
  
  def unexploredStartLocations():Iterable[TilePosition] = {
    With.game.getStartLocations.asScala.filterNot(With.game.isExplored)
  }
  
  def mostBaselikeEnemyUnit:Option[EnemyUnitInfo] = {
    knownEnemyUnits
      .toList
      .sortBy(unit => unit.getType.isFlyer)
      .sortBy(unit => ! unit.getType.isBuilding)
      .sortBy(unit => ! With.map.isTownHall(unit.getType))
      .headOption
  }
  
  def mostUnscoutedBases():Iterable[BaseLocation] = {
    BWTA.getBaseLocations.asScala
      .sortBy(base => ! base.isStartLocation)
      .sortBy(base => With.game.isExplored(base.getTilePosition))
  }
}
