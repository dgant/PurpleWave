package Global.Information.UnitAbstraction

import Startup.With
import Types.UnitInfo.EnemyUnitInfo
import bwapi.UnitType

import scala.collection.mutable

class EnemyUnitTracker {
  
  val _knownEnemyUnits = new mutable.HashMap[Integer, EnemyUnitInfo].empty
  def units:Iterable[EnemyUnitInfo] = _knownEnemyUnits.values
  
  def onFrame() {
    val trackedVisibleUnits = units
      .filter(unitInfo => With.enemyUnits.exists(_.getID == unitInfo.id))
    
    val trackedInvalidUnits = units
      .filter(unitInfo => unitInfo.unit.exists( ! _isValidEnemyUnit(_)))
    
    val trackedRelocatedUnits = units
      .filter(_._possiblyStillThere)
      .filter(trackedUnit => With.game.isVisible(trackedUnit.tilePosition))
      .filterNot(trackedUnit => trackedUnit.unit.isDefined)
    
    val untrackedVisibleUnits = With.enemyUnits
      .filter(unit => ! _knownEnemyUnits.contains(unit.getID))
      .filter(_isValidEnemyUnit)
    
    trackedVisibleUnits.foreach(unitInfo => With.unit(unitInfo.id).foreach(unitInfo.update(_)))
    trackedRelocatedUnits.foreach(_updateMissing)
    trackedInvalidUnits.foreach(_remove)
    untrackedVisibleUnits.foreach(_add)
  }
  
  def onUnitDestroy(unit:bwapi.Unit) {
    _knownEnemyUnits.get(unit.getID).foreach(_remove)
  }
  
  def _add(unit:bwapi.Unit) {
    val knownUnit = new EnemyUnitInfo(unit)
    _knownEnemyUnits.put(knownUnit.id, new EnemyUnitInfo(unit))
  }
  
  def _updateMissing(unit:EnemyUnitInfo) {
    if (unit.unitType.canMove) {
      unit.invalidatePosition()
    } else {
      //Well, if it can't move, it must be dead. Like a building that burned down or was otherwise destroyed
      _remove(unit)
      //TODO: Count that unit as dead in the score
    }
  }
  
  def _remove(unit:EnemyUnitInfo) {
    _knownEnemyUnits.remove(unit.id)
  }
  
  def _remove(id:Int) {
    _knownEnemyUnits.remove(id)
  }
  
  def _isValidEnemyUnit(unit:bwapi.Unit):Boolean = {
    unit.getPlayer.isEnemy(With.game.self) &&
      ! List(UnitType.None, UnitType.Unknown, UnitType.Zerg_Larva, UnitType.Zerg_Egg, UnitType.Resource_Vespene_Geyser).contains(unit.getType)
  }
}
