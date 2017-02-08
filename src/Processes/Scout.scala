package Processes

import Startup.With
import Types.EnemyUnitInfo
import bwapi.UnitType
import bwta.{BWTA, BaseLocation}

import scala.collection.JavaConverters._
import scala.collection.mutable

class Scout {
  
  val _knownEnemyUnits = new mutable.HashMap[Integer, EnemyUnitInfo].empty
  
  def onFrame() {
    //Stop tracking units that aren't enemies
    //Vespene geysers, add-ons, mind-controlled units, etc.
    With.game.getAllUnits.asScala
      .filter(unit => _knownEnemyUnits.contains(unit.getID))
      .filterNot(unit => unit.getPlayer.isEnemy(With.game.self))
      .foreach(unit => _knownEnemyUnits.remove(unit.getID))
      
    //Track (reasonable) enemy units
    With.game.getPlayers.asScala
      .filter(_.isEnemy(With.game.self))
      .filterNot(unit => List(
        UnitType.None,
        UnitType.Unknown,
        UnitType.Zerg_Larva,
        UnitType.Zerg_Egg)
        .contains(unit.getType))
      .flatten(_.getUnits.asScala)
      .filterNot(unit => _knownEnemyUnits.contains(unit.getID))
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
  
  def onUnitDestroy(unit:bwapi.Unit) {
    _knownEnemyUnits.remove(unit.getID)
  }
  
  def enemyUnits:Iterable[EnemyUnitInfo] = {
    _knownEnemyUnits.values
  }
  
  //someBuilding.getType.isResourceDepot seems to fail when a hatchery starts to morph
  val _townHallTypes = Set(
    UnitType.Terran_Command_Center,
    UnitType.Protoss_Nexus,
    UnitType.Zerg_Hatchery,
    UnitType.Zerg_Lair,
    UnitType.Zerg_Hive
  )
  
  def nextEnemyBase:Option[EnemyUnitInfo] = {
    enemyUnits
      .toList
      .sortBy(unit => ! _townHallTypes.contains(unit.getType))
      .sortBy(unit => ! unit.getType.isBuilding)
      .headOption
  }
  
  def unscoutedBases():Iterable[BaseLocation] = {
    BWTA.getBaseLocations.asScala
      .sortBy(base => With.game.isExplored(base.getTilePosition))
      .sortBy(base => ! base.isStartLocation)
  }
}
