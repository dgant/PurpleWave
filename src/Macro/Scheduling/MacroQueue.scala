package Macro.Scheduling

import Lifecycle.With
import Macro.BuildRequests.BuildRequest
import Macro.Buildables.Buildable
import Planning.Plan
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
  
  def queue: Vector[Buildable] = {
    val requestQueue    = requestsByPlan.keys.toVector.sortBy(_.priority).flatten(requestsByPlan)
    val unitsWanted     = new CountMap[UnitClass]
    val unitsCounted    = new CountMap[UnitClass]
    val unitsExisting   = new CountMap[UnitClass]
    val upgradesCounted = new CountMap[Upgrade]
    val techsCounted    = new mutable.HashSet[Tech]

    // Count units
    With.units.ours.foreach(unit => {
      // Use the standard macro counter to ensure production plans complete as we intend
      MacroCounter.countComplete(unit).foreach(p => unitsCounted(p._1) += p._2)
      // Require completion of Terran buildings
      MacroCounter.countCompleteOrIncomplete(unit).foreach(p => unitsExisting(p._1) += p._2)
    })
    unitsExisting.foreach(pair => unitsWanted(pair._1) = Math.max(unitsWanted(pair._1), pair._2))

    // Count upgrades and tech
    Upgrades.all.map(u => (u, With.self.getUpgradeLevel(u))).foreach(u => upgradesCounted(u._1) = u._2)
    techsCounted ++= Techs.all.filter(With.self.hasTech)

    requestQueue.flatten(getUnfulfilledBuildables(_, unitsWanted, unitsCounted, upgradesCounted, techsCounted))
  }
  
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
      val unitCountActual = unitsCounted(unit)
      unitsWanted.put(unit, Math.max(unitsWanted(unit), request.total))
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
