package ProxyBwapi.UnitTracking

import Lifecycle.With
import Performance.Caching.Limiter
import ProxyBwapi.Players.Players
import ProxyBwapi.UnitInfo.ForeignUnitInfo

import scala.collection.JavaConverters._
import scala.collection.immutable.HashSet
import scala.collection.mutable

class ForeignUnitTracker {
  
  private val foreignUnitsById = new mutable.HashMap[Int, ForeignUnitInfo].empty
  
  var foreignUnits    : Set[ForeignUnitInfo] = new HashSet[ForeignUnitInfo]
  var enemyUnits      : Set[ForeignUnitInfo] = new HashSet[ForeignUnitInfo]
  var neutralUnits    : Set[ForeignUnitInfo] = new HashSet[ForeignUnitInfo]
  var enemyGhostUnits : Set[Int]             = new HashSet[Int]
  
  def get(someUnit  : bwapi.Unit) : Option[ForeignUnitInfo] = get(someUnit.getID)
  def get(id        : Int)        : Option[ForeignUnitInfo] = foreignUnitsById.get(id)
  
  private val limitInvalidatePixels = new Limiter(1, invalidatePositions)
  def update() {
    initialize()
    
    //Important to remember: bwapi.Units are not persisted frame-to-frame
    //So we do all our comparisons by ID, rather than by object
  
    val foreignUnitsKnown     = foreignUnitsById
    val foreignUnitsVisible   = With.game.getAllUnits.asScala.filter(isValidForeignUnit).map(unit => (unit.getID, unit)).toMap
    val foreignIdsKnown       = foreignUnitsKnown.keySet
    val foreignIdsVisible     = foreignUnitsVisible.keySet
    val unitsToAdd            = foreignIdsVisible.diff      (foreignIdsKnown)   .map(foreignUnitsVisible)
    val unitsToUpdate         = foreignIdsVisible.intersect (foreignIdsKnown)   .map(foreignUnitsVisible)
    val unitsToFlagInvisible  = foreignIdsKnown.diff        (foreignIdsVisible) .map(foreignUnitsById)
    
    unitsToAdd.foreach(add)
    unitsToUpdate.foreach(unit => foreignUnitsById(unit.getID).update(unit))
  
    //Remove no-longer-valid units
    //Whoops, foreignUnitsNew already lacks these units. Maybe this step isn't necessary
    //val foreignUnitsInvalid = foreignUnitsNew.values.filterNot(_isValidForeignUnit)
    //foreignUnitsInvalid.foreach(_remove)
  
    //Could speed things up by diffing instead of recreating these
    foreignUnits = foreignUnitsById.values.toSet
    enemyUnits   = foreignUnits.filter(_.player.isEnemy)
    neutralUnits = foreignUnits.filter(_.player.isNeutral)
  
    unitsToFlagInvisible.foreach(_.flagInvisible())
    limitInvalidatePixels.act()
  }
  
  def onUnitDestroy(unit:bwapi.Unit) {
    foreignUnitsById.get(unit.getID).foreach(remove)
  }
  
  private def invalidatePositions() {
    foreignUnits.filter(unit =>
      unit.possiblyStillThere
      &&  ! unit.visible
      &&  ! unit.effectivelyCloaked
      &&  unit.tileArea.tiles.forall(tile => With.game.isVisible(tile.bwapi)))
      .foreach(updateMissing)
  }
  
  private def initialize() {
    if (With.frame == 0) {
      flagGhostUnits()
      trackStaticUnits()
    }
  }
  
  private def flagGhostUnits() {
    //At the start of the game BWAPI sometimes gives us enemy units that don't make any sense.
    val ghostUnits = With.game.getAllUnits.asScala.filter(unit => Players.get(unit.getPlayer).isEnemy)
    enemyGhostUnits = ghostUnits.map(_.getID).toSet
    if (ghostUnits.nonEmpty) {
      With.logger.warn("Found ghost units at start of game:")
      ghostUnits.map(u => u.getType + ", " + u.getPlayer.getName + " " + u.getPosition).foreach(With.logger.warn)
    }
  }
  
  private def trackStaticUnits() {
    With.game.getStaticNeutralUnits.asScala.foreach(add)
  }
  
  private def add(unit: bwapi.Unit) {
    val knownUnit = new ForeignUnitInfo(unit)
    knownUnit.update(unit)
    foreignUnitsById.put(knownUnit.id, knownUnit)
  }
  
  private def updateMissing(unit: ForeignUnitInfo) {
    //Well, if it can't move, it must be dead. Like a building that burned down or was otherwise destroyed.
    if (unit.unitClass.canMove) unit.flagMissing() else remove(unit)
  }
  
  private def remove(unit: ForeignUnitInfo) {
    unit.flagDead()
    foreignUnitsById.remove(unit.id)
  }
  
  private def remove(unit: bwapi.Unit) {
    remove(unit.getID)
  }
  
  private def remove(id: Int) {
    foreignUnitsById.remove(id)
  }
  
  private def isValidForeignUnit(unit: bwapi.Unit):Boolean = {
    //This case just doesn't make sense; if they're invisible and foreign how is BWAPI returning them
    //This check filters out the weird ghost units that BWAPI gives us at the start of a game
    if (!unit.isVisible) return false
    
    if (With.units.invalidUnitTypes.contains(unit.getType)) return false
    if ( ! unit.exists) return false
    Players.get(unit.getPlayer).isEnemy || Players.get(unit.getPlayer).isNeutral
  }
}
