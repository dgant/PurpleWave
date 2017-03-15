package Macro.Scheduling

import Planning.Plan
import Startup.With
import Macro.BuildRequests.BuildRequest
import Macro.Buildables.Buildable
import Macro.Scheduling.Optimization.{ScheduleSimulationResult, ScheduleSimulator}
import Performance.Caching.Limiter
import Utilities.{CountMap, CountMapper}
import bwapi.UnitType

import scala.collection.mutable

class Scheduler {
  
  private val requestsByPlan = new mutable.HashMap[Plan, Iterable[BuildRequest]]
  private val recentlyUpdated = new mutable.HashSet[Plan]
  
  var simulationResults:ScheduleSimulationResult = new ScheduleSimulationResult(List.empty, List.empty, List.empty)
  
  var queueOriginal   : Iterable[Buildable]   = List.empty
  def queueOptimized  : Iterable[BuildEvent]  = simulationResults.suggestedEvents
  
  def request(requester:Plan, requests: Iterable[BuildRequest]) {
    requestsByPlan.put(requester, requests)
    recentlyUpdated.add(requester)
  }
  
  def onFrame() = {
    requestsByPlan.keySet.diff(recentlyUpdated).foreach(requestsByPlan.remove)
    recentlyUpdated.clear()
    updateQueueLimiter.act()
  }
  private val updateQueueLimiter = new Limiter(2, () => updateQueue())
  private def updateQueue() {
    val unitsWanted = new CountMap[UnitType]
    val unitsActual:CountMap[UnitType] = CountMapper.make(With.units.ours.groupBy(_.utype).mapValues(_.size))
    queueOriginal = requestsByPlan.keys.toList
      .sortBy(With.prioritizer.getPriority)
      .flatten(requestsByPlan)
      .flatten(buildable => getUnfulfilledBuildables(buildable, unitsWanted, unitsActual))
    
    simulationResults = ScheduleSimulator.simulate(queueOriginal)
  }
  
  private def getUnfulfilledBuildables(
    request:BuildRequest,
    unitsWanted:CountMap[UnitType],
    unitsActual:CountMap[UnitType]):Iterable[Buildable] = {
    if (request.buildable.upgradeOption.nonEmpty) {
      if(With.self .getUpgradeLevel(request.buildable.upgradeOption.get) >= request.buildable.upgradeLevel)
        return List(request.buildable)
      else
        return None
    }
    else if (request.buildable.techOption.nonEmpty) {
      if (With.self.hasResearched(request.buildable.techOption.get))
        return List(request.buildable)
      else
        return None
    }
    else {
      val unitType = request.buildable.unitOption.get
      unitsWanted.add(unitType, request.add)
      unitsWanted.put(unitType, Math.max(request.require, unitsWanted(unitType)))
      val difference = unitsWanted(unitType) - unitsActual(unitType)
      if (difference > 0) {
        val buildables = (0 until difference).map(i => request.buildable)
        return buildables
      }
      else
        return List.empty
    }
  }
}
