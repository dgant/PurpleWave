package ProxyBwapi.UnitTracking

import Lifecycle.With
import ProxyBwapi.Players.Players
import ProxyBwapi.Races.Terran
import ProxyBwapi.UnitInfo.{ForeignUnitInfo, Orders}

import scala.collection.JavaConverters._
import scala.collection.mutable

class ForeignUnitTracker {
  
  private val unitsByIdKnown = new mutable.HashMap[Int, ForeignUnitInfo].empty
  
  var enemyUnits: Iterable[ForeignUnitInfo] = Iterable.empty
  var neutralUnits: Iterable[ForeignUnitInfo] = Iterable.empty

  var initialized = false

  def get(id: Int): Option[ForeignUnitInfo] = unitsByIdKnown.get(id)

  def update() {
    initialize()

    val unitsByIdVisible = Players.all.filterNot(_.isFriendly).flatMap(_.rawUnits)
      .map(unit => (unit.getID, unit))
      .filter(_._2.exists())

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

    enemyUnits   = unitsByIdKnown.values.filter(_.player.isEnemy)
    neutralUnits = unitsByIdKnown.values.filter(_.player.isNeutral)
  }

  def onUnitDestroy(unit: bwapi.Unit) {
    unitsByIdKnown.get(unit.getID).foreach(remove)
  }

  private def initialize() {
    if ( ! initialized) {
      initialized = true
      trackStaticUnits()
    }
  }
  
  private def trackStaticUnits() {
    val staticNeutralUnits = With.game.getStaticNeutralUnits.asScala
    if (staticNeutralUnits.size < 18) {
      With.logger.warn("Encountered surprisingly few static neutral units: " + staticNeutralUnits.size)
      staticNeutralUnits.foreach(u => With.logger.warn(u.getType.toString))
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
      if (!shouldBeDetected) {
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

      // Well, if it can't move, it must be dead. Like a building that burned down or was otherwise destroyed.
      // HACK: Vision grids can be out of date, causing us to think a unit is dead when we actually just can't see it
      // The temporary fix is querying all the tiles to ensure it's not just in the fog
      if ( ! unit.unitClass.canMove && ! unit.isSiegeTankSieged()) {
        if (unit.tileArea.tiles.exists(_.bwapiVisible)) {
          remove(unit)
        }
      } else {
        unit.flagMissing()
      }
    }
  }
  
  private def remove(unit: ForeignUnitInfo) {
    unit.flagDead()
    unitsByIdKnown.remove(unit.id)
    With.units.historicalUnitTracker.add(unit)
  }
}
