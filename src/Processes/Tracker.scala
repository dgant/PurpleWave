package Processes

import Startup.With
import bwapi.UnitType
import Types.EnemyUnitInfo

import scala.collection.mutable

class Tracker {
  
  val _knownEnemyUnits = new mutable.HashMap[Integer, EnemyUnitInfo].empty
  
  def knownEnemyUnits:Iterable[EnemyUnitInfo] = {
    _knownEnemyUnits.values
  }
  
  def onFrame() {
    val trackedVisibleUnits = knownEnemyUnits
      .filter(trackedUnit => With.unit(trackedUnit.getID).exists(_.isVisible))
    
    val trackedInvalidUnits = knownEnemyUnits
      .filter(trackedUnit => With.unit(trackedUnit.getID).exists( ! _isValidEnemyUnit(_)))
    
    val trackedRelocatedUnits = knownEnemyUnits
      .filter(_.possiblyStillThere)
      .filter(trackedUnit => With.game.isVisible(trackedUnit.getTilePosition))
      .filterNot(trackedUnit => With.unit(trackedUnit.getID).isDefined)
    
    val untrackedVisibleUnits = With.enemyUnits
      .filter(unit => ! _knownEnemyUnits.contains(unit.getID))
      .filter(_isValidEnemyUnit)
    
    trackedVisibleUnits.foreach(_updateVisibleTrackedUnit)
    trackedRelocatedUnits.foreach(_updateMissingTrackedUnit)
    trackedInvalidUnits.foreach(_removeTrackedUnit)
    untrackedVisibleUnits.foreach(_trackUnit)
  }
  
  def onUnitDestroy(unit:bwapi.Unit) {
    _knownEnemyUnits.get(unit.getID).foreach(_removeTrackedUnit)
  }
  
  def _updateVisibleTrackedUnit(trackedUnit:EnemyUnitInfo) {
    val unit = With.unit(trackedUnit.getID).get
    trackedUnit.lastSeen            = With.game.getFrameCount
    trackedUnit.possiblyStillThere  = true
    trackedUnit.getPlayer           = unit.getPlayer
    trackedUnit.getPosition         = unit.getPosition
    trackedUnit.getTilePosition     = unit.getTilePosition
    trackedUnit.getHitPoints        = unit.getHitPoints
    trackedUnit.getShields          = unit.getShields
    trackedUnit.getType             = unit.getType
    trackedUnit.isCompleted         = unit.isCompleted
  }
  
  def _updateMissingTrackedUnit(unit:EnemyUnitInfo) {
    if (unit.getType.canMove) {
      unit.possiblyStillThere = false
    } else {
      //A building that burned down or was otherwise destroyed
      _removeTrackedUnit(unit)
    }
  }
  
  def _removeTrackedUnit(unit:EnemyUnitInfo) {
    _knownEnemyUnits.remove(unit.getID)
  }
  
  def _trackUnit(unit:bwapi.Unit) {
    val knownUnit =
      new EnemyUnitInfo(
        getID               = unit.getID,
        lastSeen            = With.game.getFrameCount,
        possiblyStillThere  = true,
        getPlayer           = unit.getPlayer,
        getPosition         = unit.getPosition,
        getTilePosition     = unit.getTilePosition,
        getHitPoints        = unit.getHitPoints,
        getShields          = unit.getShields,
        getType             = unit.getType,
        isCompleted         = unit.isCompleted)
    _knownEnemyUnits.put(knownUnit.getID, knownUnit)
  }
  
  def _isValidEnemyUnit(unit:bwapi.Unit):Boolean = {
    unit.getPlayer.isEnemy(With.game.self) &&
      ! List(UnitType.None, UnitType.Unknown, UnitType.Zerg_Larva, UnitType.Zerg_Egg, UnitType.Resource_Vespene_Geyser).contains(unit.getType)
  }
  
  def _removeKnownUnit(id:Int) {
    _knownEnemyUnits.remove(id)
  }
}
