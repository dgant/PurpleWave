package ProxyBwapi.UnitTracking

import Lifecycle.With
import ProxyBwapi.Players.Players
import ProxyBwapi.Races.Terran
import ProxyBwapi.UnitInfo.{ForeignUnitInfo, Orders}

import scala.collection.JavaConverters._
import scala.collection.immutable.HashSet
import scala.collection.mutable

class ForeignUnitTracker {
  
  private val unitsByIdKnown = new mutable.HashMap[Int, ForeignUnitInfo].empty
  
  var foreignUnits    : Set[ForeignUnitInfo] = new HashSet[ForeignUnitInfo]
  var enemyUnits      : Set[ForeignUnitInfo] = new HashSet[ForeignUnitInfo]
  var neutralUnits    : Set[ForeignUnitInfo] = new HashSet[ForeignUnitInfo]
  var enemyGhostUnits : Set[Int]             = new HashSet[Int]
  
  def get(someUnit  : bwapi.Unit) : Option[ForeignUnitInfo] = get(someUnit.getID)
  def get(id        : Int)        : Option[ForeignUnitInfo] = unitsByIdKnown.get(id)
  
  def update() {
    initialize()
  
    val unitsByIdVisible      = Players.all.filterNot(_.isFriendly).flatMap(_.rawUnits).filter(isValidForeignUnit).map(unit => (unit.getID, unit)).toMap
    val unitsToAdd            = unitsByIdVisible.toSeq.filterNot(pair => unitsByIdKnown   .contains(pair._1))
    val unitsToUpdate         = unitsByIdVisible.toSeq.filter   (pair => unitsByIdKnown   .contains(pair._1))
    val unitsToFlagInvisible  = unitsByIdKnown  .toSeq.filterNot(pair => unitsByIdVisible .contains(pair._1))
    
    unitsToAdd.foreach(pair => add(pair._2))
    unitsToUpdate.foreach(pair => unitsByIdKnown(pair._1).update(pair._2))
  
    //Could speed things up by diffing instead of recreating these
    foreignUnits = unitsByIdKnown.values.toSet
    enemyUnits   = foreignUnits.filter(_.player.isEnemy)
    neutralUnits = foreignUnits.filter(_.player.isNeutral)
  
    unitsToFlagInvisible.foreach(_._2.flagInvisible())
    invalidatePositions()
  }
  
  def onUnitDestroy(unit: bwapi.Unit) {
    unitsByIdKnown.get(unit.getID).foreach(remove)
  }
  
  private def invalidatePositions() {
    foreignUnits.foreach(updateMissing)
  }
  
  private def initialize() {
    if (With.frame == 0) {
      flagGhostUnits()
      trackStaticUnits()
    }
  }
  
  private def flagGhostUnits() {
    if ( ! With.configuration.identifyGhostUnits) {
      return
    }
    //At the start of the game BWAPI sometimes gives us enemy units that don't make any sense.\
    // TODO: Track ghost minerals too
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
    unitsByIdKnown.put(knownUnit.id, knownUnit)
  }
  
  private def updateMissing(unit: ForeignUnitInfo) {
  
    if (unit.visible)                                              return
    if ( ! unit.possiblyStillThere)                                return
    if (unit.lastSeen > With.grids.friendlyVision.frameUpdated)    return
    if (unit.lastSeen > With.grids.friendlyDetection.frameUpdated) return
    if (With.framesSince(unit.lastSeen) < 24 * 2)                  return
    
    lazy val shouldBeVisible  = With.grids.friendlyVision.isSet(unit.tileIncludingCenter)
    lazy val shouldBeDetected = With.grids.friendlyDetection.isSet(unit.tileIncludingCenter)
    lazy val shouldUnburrow   = unit.is(Terran.SpiderMine) && With.units.inTileRadius(unit.tileIncludingCenter, 3).exists(unit => unit.isOurs  && ! unit.flying && ! unit.unitClass.floats)
    lazy val wasBurrowing     = unit.burrowed || Array(Orders.Burrowing, Orders.VultureMine).contains(unit.order)
    lazy val wasCloaking      = unit.cloaked  || unit.order == Orders.Cloak
  
    if (shouldUnburrow) {
      remove(unit)
    }
    else if (shouldBeVisible) {
      if ( ! shouldBeDetected) {
        if (wasBurrowing) {
          unit.flagBurrowed()
          unit.flagUndetected()
          return
        }
        else if (wasCloaking) {
          unit.flagCloaked()
          unit.flagUndetected()
          return
        }
      }
  
      //Well, if it can't move, it must be dead. Like a building that burned down or was otherwise destroyed.
      if (unit.unitClass.canMove) unit.flagMissing() else remove(unit)
    }
  }
  
  private def remove(unit: ForeignUnitInfo) {
    unit.flagDead()
    unitsByIdKnown.remove(unit.id)
  }
  
  private def remove(unit: bwapi.Unit) {
    remove(unit.getID)
  }
  
  private def remove(id: Int) {
    unitsByIdKnown.remove(id)
  }
  
  private def isValidForeignUnit(unit: bwapi.Unit): Boolean = {
    if ( ! unit.exists) {
      return false
    }
    
    if (With.configuration.identifyGhostUnits) {
      val id = unit.getID
      if (enemyGhostUnits.contains(id)) {
        if (With.frame > 5 && unit.isVisible) {
          // Looks like it's a legit unit! It just happened to share an ID with a ghost unit
          enemyGhostUnits = enemyGhostUnits - id
        }
        else {
          return false
        }
      }
    }
    
    true
  }
}
