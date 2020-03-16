package Macro.Scheduling

import Lifecycle.With
import Macro.BuildRequests.BuildRequest
import Macro.Buildables.Buildable
import Performance.Cache
import Planning.Plan
import ProxyBwapi.Races.{Terran, Zerg}
import ProxyBwapi.Techs.{Tech, Techs}
import ProxyBwapi.UnitClasses.UnitClass
import ProxyBwapi.Upgrades.{Upgrade, Upgrades}
import Utilities.CountMap

import scala.collection.mutable

class MacroQueue {
  
  val requestsByPlan = new mutable.HashMap[Plan, Iterable[BuildRequest]]
  
  def reset() {
    requestsByPlan.clear()
  }
  
  def request(requester: Plan, theRequest: BuildRequest) {
    request(requester, Iterable(theRequest))
  }

  def request(requester: Plan, requests: Iterable[BuildRequest]): Unit = {
    requestsByPlan.put(requester, requestsByPlan.getOrElse(requester, Iterable.empty) ++ requests)
  }
  
  def audit: Vector[(Plan, Iterable[BuildRequest])] = {
    requestsByPlan.toVector.sortBy(_._1.priority)
  }
  
  def queue: Vector[Buildable] = queueCache()
  val queueCache = new Cache[Vector[Buildable]](() => {
    val requestQueue = requestsByPlan.keys.toVector.sortBy(_.priority).flatten(requestsByPlan)
    val unitsWanted = new CountMap[UnitClass]
    val unitsCounted = new CountMap[UnitClass]
    val upgradesCounted = new CountMap[Upgrade]
    val techsCounted = new mutable.HashSet[Tech]
    Upgrades.all.map(u => (u, With.self.getUpgradeLevel(u))).foreach(u => upgradesCounted(u._1) = u._2)
    techsCounted ++= Techs.all.filter(With.self.hasTech)
    With.units.ours.foreach(unit => {
      // Count the unit if it no longer needs attention
      // Namely, Terran buildings need ongoing support
      if (unit.complete || ! (unit.unitClass.isBuilding && unit.unitClass.isTerran)) {
        unitsCounted.add(unit.unitClass, 1)
      }
      if (unit.is(Terran.SiegeTankSieged)) {
        unitsCounted.add(Terran.SiegeTankUnsieged, 1)
      }
      if (unit.is(Zerg.GreaterSpire)) {
        unitsCounted.add(Zerg.Spire, 1)
      }
      if (unit.is(Zerg.Lair)) {
        unitsCounted.add(Zerg.Hatchery, 1)
      }
      if (unit.is(Zerg.Hive)) {
        unitsCounted.add(Zerg.Lair, 1)
        unitsCounted.add(Zerg.Hatchery, 1)
      }
    })
    // Don't leave Terran buildings incomplete;
    // Make sure we have a plan for finishing all our existing buildings
    if (With.self.isTerran) {
      With.units.ours
        .filter(u => u.unitClass.isBuilding && ! u.complete)
        .map(_.unitClass)
        .toVector
        .distinct
        .foreach(u => unitsWanted(u) = Math.max(unitsWanted(u), With.units.countOurs(u)))
    }
    requestQueue.flatten(getUnfulfilledBuildables(_, unitsWanted, unitsCounted, upgradesCounted, techsCounted))
  })
  
  private def getUnfulfilledBuildables(
    request         : BuildRequest,
    unitsWanted     : CountMap[UnitClass],
    unitsCounted    : CountMap[UnitClass],
    upgradesCounted : CountMap[Upgrade],
    techsCounted    : mutable.Set[Tech])
      : Iterable[Buildable] = {
    
    if (request.buildable.upgradeOption.nonEmpty) {
      val upgrade = request.buildable.upgradeOption.get
      if (With.self.getUpgradeLevel(upgrade) < request.buildable.upgradeLevel) {
        upgradesCounted(upgrade) = request.buildable.upgradeLevel
        Vector(request.buildable)
      } else if (request.add > 0 && upgradesCounted(upgrade) < upgrade.levels.last) {
        upgradesCounted(upgrade) += 1
        Vector(request.buildable)
      } else {
        None
      }
    }
    else if (request.buildable.techOption.nonEmpty) {
      val tech = request.buildable.techOption.get
      if (techsCounted.contains(tech)) {
        None
      } else {
        techsCounted += tech
        Vector(request.buildable)
      }
    }
    else {
      val unit = request.buildable.unitOption.get
      var unitCountActual = unitsCounted(unit)
      unitsWanted.put(unit, request.add + Math.max(unitsWanted(unit), unitCountActual))
      unitsWanted.put(unit, Math.max(unitsWanted(unit), request.require))
      var difference = unitsWanted(unit) - unitCountActual
      if (unit.isTwoUnitsInOneEgg) {
        difference = (1 + difference) / 2
      }
      if (difference > 0) {
        unitsCounted(unit) += difference
        val buildables = (0 until difference).map(i => request.buildable)
        buildables
      }
      else {
        Vector.empty
      }
    }
  }
}
