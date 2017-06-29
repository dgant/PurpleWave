package Macro.Scheduling

import Lifecycle.With
import Macro.BuildRequests.BuildRequest
import Macro.Buildables.Buildable
import Macro.Scheduling.Optimization.ScheduleSimulationResult
import Performance.Caching.Limiter
import Planning.Plan
import ProxyBwapi.UnitClass.UnitClass
import Utilities.{CountMap, CountMapper}

import scala.collection.mutable

class Scheduler {
  
  private val requestsByPlan  = new mutable.HashMap[Plan, Iterable[BuildRequest]]
  private val recentlyUpdated = new mutable.HashSet[Plan]
  
  var simulationResults:ScheduleSimulationResult = new ScheduleSimulationResult(Vector.empty, Vector.empty, Vector.empty)
  
  var queueOriginal   : Iterable[Buildable]   = Vector.empty
  def queueOptimized  : Iterable[BuildEvent]  = simulationResults.suggestedEvents
  
  def request(requester: Plan, aRequest: BuildRequest) {
    request(requester, List(aRequest))
  }
  
  def request(requester: Plan, requests: Iterable[BuildRequest]) {
    requestsByPlan.put(requester, requests)
    recentlyUpdated.add(requester)
  }
  
  def update() {
    requestsByPlan.keySet.diff(recentlyUpdated).foreach(requestsByPlan.remove)
    
    //TODO: This needs to go elsewhere when we go async!
    recentlyUpdated.clear()
    updateQueueLimiter.act()
  }
  
  private val updateQueueLimiter = new Limiter(2, () => updateQueue())
  private def updateQueue() {
    val requestQueue = requestsByPlan.keys.toVector.sortBy(_.priority).flatten(requestsByPlan)
    val unitsWanted = new CountMap[UnitClass]
    val unitsActual:CountMap[UnitClass] = CountMapper.make(With.units.ours.filter(_.aliveAndComplete).groupBy(_.unitClass).mapValues(_.size))
    queueOriginal = requestQueue.flatten(buildable => getUnfulfilledBuildables(buildable, unitsWanted, unitsActual))
    //simulationResults = ScheduleSimulator.simulate(queueOriginal)
  }
  
  private def getUnfulfilledBuildables(
    request:BuildRequest,
    unitsWanted:CountMap[UnitClass],
    unitsActual:CountMap[UnitClass])
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
        Vector(request.buildable)
      else
        None
    }
    else {
      val unit = request.buildable.unitOption.get
      val differenceBefore = Math.max(0, unitsWanted(unit) - unitsActual(unit))
      unitsWanted.put(unit, Math.max(unitsWanted(unit), unitsActual(unit) + request.add))
      unitsWanted.put(unit, Math.max(unitsWanted(unit), request.require))
      val differenceAfter = unitsWanted(unit) - unitsActual(unit)
      val differenceChange = differenceAfter - differenceBefore
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
