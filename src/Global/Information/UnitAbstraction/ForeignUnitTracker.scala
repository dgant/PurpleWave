package Global.Information.UnitAbstraction

import Startup.With
import Types.UnitInfo.ForeignUnitInfo
import bwapi.UnitType

import scala.collection.JavaConverters._
import scala.collection.immutable.HashSet
import scala.collection.mutable

class ForeignUnitTracker {
  
  val _foreignUnitsById = new mutable.HashMap[Int, ForeignUnitInfo].empty
  var _foreignUnits:Set[ForeignUnitInfo] = new HashSet[ForeignUnitInfo]
  var _enemyUnits:Set[ForeignUnitInfo] = new HashSet[ForeignUnitInfo]
  
  def enemyUnits:Set[ForeignUnitInfo] = _enemyUnits
  def get(someUnit:bwapi.Unit):Option[ForeignUnitInfo] = get(someUnit.getID)
  def get(id:Int):Option[ForeignUnitInfo] = _foreignUnitsById.get(id)
  
  def onFrame() {
  
    //Important to remember: bwapi.Units are not persisted frame-to-frame
    //So we do all our comparisons by ID, rather than by object
    
    val foreignUnitsNew           = With.game.getAllUnits.asScala.filter(_isValidForeignUnit).map(unit => (unit.getID, unit)).toMap
    val foreignUnitsOld           = _foreignUnitsById
    val foreignIdsNew             = foreignUnitsNew.keySet
    val foreignIdsOld             = foreignUnitsOld.keySet
    val unitsToAdd                = foreignIdsNew.diff(foreignIdsOld).map(foreignUnitsNew)
    val unitsToUpdate             = foreignIdsNew.intersect(foreignIdsOld).map(foreignUnitsNew)
    val unitsToInvalidatePosition = foreignIdsOld.diff(foreignIdsNew)
      .map(foreignUnitsOld)
      .filter(_.possiblyStillThere) //This check is important! It makes the O(n^2) filter at the end O(n)
      .filter(unitInfo => With.game.isVisible(unitInfo.tilePosition))
    
    unitsToAdd.foreach(_add)
    unitsToUpdate.foreach(unit => _foreignUnitsById(unit.getID).update(unit))
    unitsToInvalidatePosition.foreach(_updateMissing)
  
    //Remove no-longer-valid units
    //We have to do this after updating because it needs the latest bwapi.Units
    //val noLongerValid = foreignUnitsNow.values.filterNot(unitInfo => _isValidForeignUnit(unitInfo.baseUnit))
    //noLongerValid.foreach(_remove)
  
    //Could speed things up by diffing instead of recreating these
    _foreignUnits = _foreignUnitsById.values.toSet
    _enemyUnits = _foreignUnits.filter(_.player.isEnemy(With.game.self))
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
    if (List(UnitType.None, UnitType.Unknown).contains(unit.getType)) return false
    if ( ! unit.exists) return false
    unit.getPlayer.isEnemy(With.game.self) || unit.getPlayer.isNeutral
  }
}
