package Macro.Scheduling

import Lifecycle.With
import Macro.BuildRequests.BuildRequest
import Macro.Buildables.Buildable
import Performance.Caching.CacheFrame
import Planning.Plan
import ProxyBwapi.UnitClass.UnitClass
import Utilities.{CountMap, CountMapper}

import scala.collection.mutable

class Scheduler {
  
  val requestsByPlan = new mutable.HashMap[Plan, Iterable[BuildRequest]]
  
  def request(requester: Plan, theRequest: BuildRequest) {
    request(requester, List(theRequest))
  }
  
  def request(requester: Plan, requests: Iterable[BuildRequest]) {
    requestsByPlan.put(requester, requests)
  }
  
  def reset() {
    requestsByPlan.clear()
  }
  
  def queue: Iterable[Buildable] = queueCache.get
  val queueCache = new CacheFrame(() => queueRecalculate)
  private def queueRecalculate: Iterable[Buildable] = {
    val requestQueue = requestsByPlan.keys.toVector.sortBy(_.priority).flatten(requestsByPlan)
    val unitsWanted = new CountMap[UnitClass]
    val unitsActual: CountMap[UnitClass] = CountMapper.make(With.units.ours.filter(_.aliveAndComplete).groupBy(_.unitClass).mapValues(_.size))
    requestQueue.flatten(buildable => getUnfulfilledBuildables(buildable, unitsWanted, unitsActual))
  }
  
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
      val differenceBefore = Math.max(0, unitsWanted(unit) - unitsActual(unit))
      unitsWanted.put(unit, Math.max(unitsWanted(unit), unitsActual(unit) + request.add))
      unitsWanted.put(unit, Math.max(unitsWanted(unit), request.require))
      val differenceAfter = unitsWanted(unit) - unitsActual(unit)
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
