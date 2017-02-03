package Processes

import Startup.With
import bwapi.Position
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
      .filterNot(unit => unit.exists)
      .foreach(_knownEnemyUnits.remove)
  }
  
  def enemyUnits():Iterable[bwapi.Unit] = {
    _knownEnemyUnits
  }
  
  def enemyBaseLocationPosition():Option[Position] = {
    
    val visibleBase = enemyUnits
      .filter(_.getType.isResourceDepot)
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
