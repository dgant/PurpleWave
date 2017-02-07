package Processes

import Startup.With
import bwapi.{Position, UnitType}
import bwta.{BWTA, BaseLocation}

import scala.collection.JavaConverters._
import scala.collection.mutable

class Scout {
  
  val _knownEnemyUnits:mutable.HashSet[bwapi.Unit] = mutable.HashSet.empty
  
  def onFrame() {
    With.game.getPlayers.asScala
      .filter(_.isEnemy(With.game.self))
      .flatten(_.getUnits.asScala)
      .foreach(_knownEnemyUnits.add)
    
    _knownEnemyUnits
      .foreach(_knownEnemyUnits.remove)
  }
  
  def onUnitDestroy(unit:bwapi.Unit) {
    _knownEnemyUnits.remove(unit)
  }
  
  def enemyUnits():Iterable[bwapi.Unit] = {
    _knownEnemyUnits
  }
  
  //someBuilding.getType.isResourceDepot seems to fail when a hatchery starts to morph
  val _townHallTypes = Set(
    UnitType.Terran_Command_Center,
    UnitType.Protoss_Nexus,
    UnitType.Zerg_Hatchery,
    UnitType.Zerg_Lair,
    UnitType.Zerg_Hive
  )
  
  def enemyBaseLocationPosition:Option[Position] = {
    
    val visibleBase = enemyUnits
      .filter(unit => _townHallTypes.contains(unit.getType))
      .headOption
    
    if (visibleBase.isDefined) {
      return Some(visibleBase.get.getPosition)
    }
      
    if (unexploredStartLocations.size == 1) {
      return Some(unexploredStartLocations.head.getPosition)
    }
    
    //If their base is in a non-start location, we're screwed.
    
    None
  }
  
  def unexploredStartLocations():Iterable[BaseLocation] = {
    BWTA.getStartLocations.asScala
      .filterNot(base => With.game.isExplored(base.getTilePosition))
  }
}
