package Macro.Scheduling.DumbQueue

import Lifecycle.With
import Macro.BuildRequests.BuildRequest
import Macro.Buildables.Buildable
import Performance.Cache
import Planning.Plan
import ProxyBwapi.Races.{Terran, Zerg}
import ProxyBwapi.UnitClasses.UnitClass
import Utilities.CountMap

import scala.collection.mutable

class DumbQueue {
  
  val requestsByPlan = new mutable.HashMap[Plan, Iterable[BuildRequest]]
  
  def reset() {
    requestsByPlan.clear()
  }
  
  def request(requester: Plan, requests: Iterable[BuildRequest]) {
    requestsByPlan.put(requester, requests ++ requestsByPlan.getOrElse(requester, Iterable.empty))
  }
  
  def audit: Vector[(Plan, Iterable[BuildRequest])] = {
    requestsByPlan.toVector.sortBy(_._1.priority)
  }
  
  def queue: Iterable[Buildable] = queueCache()
  val queueCache = new Cache(() => {
    val requestQueue = requestsByPlan.keys.toVector.sortBy(_.priority).flatten(requestsByPlan)
    val unitsWanted = new CountMap[UnitClass]
    val unitsActual = new CountMap[UnitClass]
      With.units.ours.foreach(unit => {
        if (unit.aliveAndComplete) {
          unitsActual.add(unit.unitClass, 1)
        }
        if (unit.is(Terran.SiegeTankSieged)) {
          unitsActual.add(Terran.SiegeTankUnsieged, 1)
        }
        if (unit.is(Zerg.GreaterSpire)) {
          unitsActual.add(Zerg.Spire, 1)
        }
        if (unit.is(Zerg.Lair)) {
          unitsActual.add(Zerg.Hatchery, 1)
        }
        if (unit.is(Zerg.Hive)) {
          unitsActual.add(Zerg.Lair, 1)
          unitsActual.add(Zerg.Hatchery, 1)
        }
      })
    requestQueue.flatten(buildable => getUnfulfilledBuildables(buildable, unitsWanted, unitsActual))
  })
  
  private def getUnfulfilledBuildables(
    request     : BuildRequest,
    unitsWanted : CountMap[UnitClass],
    unitsActual : CountMap[UnitClass])
      : Iterable[Buildable] = {
    
    if (request.buildable.upgradeOption.nonEmpty) {
      val upgrade = request.buildable.upgradeOption.get
      if (With.self.getUpgradeLevel(upgrade) < request.buildable.upgradeLevel)
        Vector(request.buildable)
      else if (request.add > 0 && With.self.getUpgradeLevel(upgrade) < upgrade.levels.last)
        Vector(request.buildable)
      else
        None
    }
    else if (request.buildable.techOption.nonEmpty) {
      if (With.self.hasTech(request.buildable.techOption.get))
        None
      else
        Vector(request.buildable)
    }
    else {
      val unit = request.buildable.unitOption.get
      var unitCountActual = unitsActual(unit)
      val differenceBefore = Math.max(0, unitsWanted(unit) - unitCountActual)
      unitsWanted.put(unit, request.add + Math.max(unitsWanted(unit), unitCountActual))
      unitsWanted.put(unit, Math.max(unitsWanted(unit), request.require))
      val differenceAfter = unitsWanted(unit) - unitCountActual
      var differenceChange = differenceAfter - differenceBefore
      if (unit.isTwoUnitsInOneEgg) {
        differenceChange = (1 + differenceChange) / 2
      }
      if (differenceChange > 0) {
        val buildables = (0 until differenceChange).map(i => request.buildable)
        buildables
      }
      else {
        Vector.empty
      }
    }
  }
}
