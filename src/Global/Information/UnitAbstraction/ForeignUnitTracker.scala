package Global.Information.UnitAbstraction

import Startup.With
import Types.UnitInfo.ForeignUnitInfo
import bwapi.UnitType

import scala.collection.JavaConverters._
import scala.collection.mutable

class ForeignUnitTracker {
  
  val _foreignUnitsById = new mutable.HashMap[Int, ForeignUnitInfo].empty
  
  def units:Iterable[ForeignUnitInfo] = _foreignUnitsById.values
  def get(someUnit:bwapi.Unit):Option[ForeignUnitInfo] = get(someUnit.getID)
  def get(id:Int):Option[ForeignUnitInfo] = _foreignUnitsById.get(id)
  
  def onFrame() {
    val unitsToTrack = With.game.getAllUnits.asScala.filter(_isValidForeignUnit)
    val unitsToUpdate = unitsToTrack.filter(unit => get(unit).nonEmpty)
    val unitsToRemove = units.filter(unitInfo => _isValidForeignUnit(unitInfo.baseUnit))
    
    val trackedRelocated = units
      .filter(_.possiblyStillThere)
      .filter(unitInfo => With.game.isVisible(unitInfo.tilePosition))
      .filterNot(unitInfo => unitsToTrack.exists(_.getID == unitInfo.id))
    
    val untrackedVisibleUnits = unitsToTrack
      .filter(unit => ! _foreignUnitsById.contains(unit.getID))
      .filter(_isValidForeignUnit)
    
    unitsToUpdate.foreach(unit => get(unit).foreach(unitInfo => unitInfo.update(unit)))
    trackedRelocated.foreach(_updateMissing)
    unitsToRemove.foreach(_remove)
    untrackedVisibleUnits.foreach(_add)
  }
  
  def onUnitDestroy(unit:bwapi.Unit) {
    _foreignUnitsById.get(unit.getID).foreach(_remove)
  }
  
  def _add(unit:bwapi.Unit) {
    val knownUnit = new ForeignUnitInfo(unit)
    _foreignUnitsById.put(knownUnit.id, new ForeignUnitInfo(unit))
  }
  
  def _updateMissing(unit:ForeignUnitInfo) {
    if (unit.unitType.canMove) {
      unit.invalidatePosition()
    } else {
      //Well, if it can't move, it must be dead. Like a building that burned down or was otherwise destroyed
      _remove(unit)
      //TODO: Count that unit as dead in the score
    }
  }
  
  def _remove(unit:ForeignUnitInfo) {
    unit.flagDead()
    _foreignUnitsById.remove(unit.id)
  }
  
  def _remove(id:Int) {
    _foreignUnitsById.remove(id)
  }
  
  def _isValidForeignUnit(unit:bwapi.Unit):Boolean = {
    if (List(UnitType.None, UnitType.Unknown).contains(unit.getType)) {
      return false
    }
    
    unit.getPlayer.isEnemy(With.game.self) || unit.getPlayer.isNeutral
  }
}
