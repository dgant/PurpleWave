package Global.Information.UnitAbstraction

import Startup.With
import Types.UnitInfo.ForeignUnitInfo

import scala.collection.JavaConverters._
import scala.collection.immutable.HashSet
import scala.collection.mutable

class ForeignUnitTracker {
  
  val _foreignUnitsById = new mutable.HashMap[Int, ForeignUnitInfo].empty
  var _foreignUnits:Set[ForeignUnitInfo] = new HashSet[ForeignUnitInfo]
  var _enemyUnits:Set[ForeignUnitInfo] = new HashSet[ForeignUnitInfo]
  var _neutralUnits:Set[ForeignUnitInfo] = new HashSet[ForeignUnitInfo]
  var _bannedEnemyUnitIds:Set[Int] = new HashSet[Int]
  
  def neutralUnits:Set[ForeignUnitInfo] = _neutralUnits
  def enemyUnits:Set[ForeignUnitInfo] = _enemyUnits
  def get(someUnit:bwapi.Unit):Option[ForeignUnitInfo] = get(someUnit.getID)
  def get(id:Int):Option[ForeignUnitInfo] = _foreignUnitsById.get(id)
  
  def onFrame() {
    _initialize()
    
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
      .filter(unitInfo => unitInfo.tileArea.tiles.forall(With.game.isVisible)) //Can we check fewer tiles?
    
    unitsToAdd.foreach(_add)
    unitsToUpdate.foreach(unit => _foreignUnitsById(unit.getID).update(unit))
    unitsToInvalidatePosition.foreach(_updateMissing)
  
    //Remove no-longer-valid units
    //Whoops, foreignUnitsNew already lacks these units. Maybe this step isn't necessary
    //val foreignUnitsInvalid = foreignUnitsNew.values.filterNot(_isValidForeignUnit)
    //foreignUnitsInvalid.foreach(_remove)
  
    //Could speed things up by diffing instead of recreating these
    _foreignUnits = _foreignUnitsById.values.toSet
    _enemyUnits = _foreignUnits.filter(_.player.isEnemy(With.game.self))
    _neutralUnits = _foreignUnits.filter(_.player.isNeutral)
  }
  
  def onUnitDestroy(unit:bwapi.Unit) {
    _foreignUnitsById.get(unit.getID).foreach(_remove)
  }
  
  def _initialize() {
    //BWAPI seems to start some games returning enemy units that don't make any sense.
    //This will let us catch them while debugging until we figure this out for good
    if (With.game.getFrameCount == 0) {
      _flagGhostUnits()
      _trackStaticUnits()
    }
  }
  
  def _flagGhostUnits() {
    val ghostUnits = With.game.getAllUnits.asScala.filter(_.getPlayer.isEnemy(With.game.self))
    _bannedEnemyUnitIds = ghostUnits.map(_.getID).toSet
    if (ghostUnits.nonEmpty) {
      With.logger.warn("Found ghost units at start of game:")
      ghostUnits.map(u => u.getType + ", " + u.getPlayer.getName + " " + u.getPosition).foreach(With.logger.warn)
    }
  }
  
  def _trackStaticUnits() {
    With.game.getStaticNeutralUnits.asScala.foreach(_add)
  }
  
  def _add(unit:bwapi.Unit) {
    val knownUnit = new ForeignUnitInfo(unit)
    _foreignUnitsById.put(knownUnit.id, new ForeignUnitInfo(unit))
  }
  
  def _updateMissing(unit:ForeignUnitInfo) {
    if (unit.utype.canMove) {
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
  
  def _remove(unit:bwapi.Unit) {
    _remove(unit.getID)
  }
  
  def _remove(id:Int) {
    _foreignUnitsById.remove(id)
  }
  
  def _isValidForeignUnit(unit:bwapi.Unit):Boolean = {
    //This case just doesn't make sense; if they're invisible and foreign how is BWAPI returning them
    //This check filters out the weird ghost units that BWAPI gives us at the start of a game
    if (!unit.isVisible) return false
    
    if (With.units.invalidUnitTypes.contains(unit.getType)) return false
    if ( ! unit.exists) return false
    unit.getPlayer.isEnemy(With.game.self) || unit.getPlayer.isNeutral
  }
}
