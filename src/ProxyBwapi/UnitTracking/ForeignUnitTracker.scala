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
  
  var enemyUnits      : Set[ForeignUnitInfo] = new HashSet[ForeignUnitInfo]
  var neutralUnits    : Set[ForeignUnitInfo] = new HashSet[ForeignUnitInfo]
  var enemyGhostUnits : Set[Int]             = new HashSet[Int]
  
  def get(id: Int): Option[ForeignUnitInfo] = unitsByIdKnown.get(id)
  
  def update() {
    initialize()

    val unitsByIdVisible = Players.all.filterNot(_.isFriendly).flatMap(_.rawUnits)
      .map(unit => (unit.getID, unit))
      .filter(isValidForeignUnit)

    unitsByIdKnown.foreach(pair => pair._2.flagInvisible())
    for (pair <- unitsByIdVisible) {
      val knownUnit = unitsByIdKnown.get(pair._1)
      if (knownUnit.isDefined) {
        knownUnit.get.flagVisible()
        knownUnit.get.update(pair._2)
      }
      else {
        add(pair._2, pair._1).flagVisible()
      }
    }

    unitsByIdKnown.values.foreach(updateMissing)
  
    // TODO: Let's stop making these sets by default.
    enemyUnits   = unitsByIdKnown.values.filter(_.player.isEnemy).toSet
    neutralUnits = unitsByIdKnown.values.filter(_.player.isNeutral).toSet
  }
  
  def onUnitDestroy(unit: bwapi.Unit) {
    unitsByIdKnown.get(unit.getID).foreach(remove)
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
    Players.all
    val ghostUnits = With.game.getAllUnits.asScala.filter(unit => Players.get(unit.getPlayer).isEnemy)
    enemyGhostUnits = ghostUnits.map(_.getID).toSet
    if (ghostUnits.nonEmpty) {
      With.logger.warn("Found ghost units at start of game:")
      ghostUnits.map(u => u.getType + ", " + u.getPlayer.getName + " " + u.getPosition).foreach(With.logger.warn)
    }
  }
  
  private def trackStaticUnits() {
    val staticNeutralUnits = With.game.getStaticNeutralUnits.asScala
    if (staticNeutralUnits.size < 18) {
      With.logger.warn("Encountered surprisingly few static neutral units: " + staticNeutralUnits.size)
    }
    staticNeutralUnits.foreach(add)
  }

  private def add(unit: bwapi.Unit): ForeignUnitInfo = {
    add(unit, unit.getID)
  }
  
  private def add(unit: bwapi.Unit, id: Int): ForeignUnitInfo = {
    val proxyUnit = new ForeignUnitInfo(unit, unit.getID)
    proxyUnit.update(unit)
    unitsByIdKnown.put(id, proxyUnit)
    proxyUnit
  }
  
  private def updateMissing(unit: ForeignUnitInfo) {
  
    if (unit.visible)                                              return
    if ( ! unit.possiblyStillThere)                                return
    if (unit.lastSeen > With.grids.friendlyVision.frameUpdated)    return
    if (unit.lastSeen > With.grids.friendlyDetection.frameUpdated) return
    if (With.framesSince(unit.lastSeen) < 24 * 1)                  return
    
    lazy val shouldBeVisible  = With.grids.friendlyVision.isSet(unit.tileIncludingCenter)
    lazy val shouldBeDetected = With.grids.friendlyDetection.isSet(unit.tileIncludingCenter)
    lazy val shouldUnburrow   = unit.burrowed && unit.is(Terran.SpiderMine) && unit.battle.isDefined && unit.matchups.enemies.exists(tripper => tripper.pixelDistanceEdge(unit) < 96 && tripper.unitClass.triggersSpiderMines)
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
    With.units.historicalUnitTracker.add(unit)
  }
  
  private def isValidForeignUnit(unitPair: (Int, bwapi.Unit)): Boolean = {
    val id = unitPair._1
    val unit = unitPair._2
    
    if ( ! unit.exists) {
      return false
    }
    
    if (With.configuration.identifyGhostUnits) {
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
