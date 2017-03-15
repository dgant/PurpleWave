package Global.Resources.Scheduling

import Plans.Plan
import Startup.With
import Types.BuildRequest.BuildRequest
import Types.Buildable.Buildable
import Utilities.Caching.Limiter
import Utilities.{CountMap, CountMapper}
import bwapi.UnitType

import scala.collection.mutable

class Scheduler {
  
  private val requestsByPlan = new mutable.HashMap[Plan, Iterable[BuildRequest]]
  private val recentlyUpdated = new mutable.HashSet[Plan]
  
  var simulationResults:ScheduleSimulationResult = new ScheduleSimulationResult(List.empty, List.empty, List.empty)
  var queue:Iterable[BuildEvent] = simulationResults.suggestedEvents
  
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
    val rawQueue = requestsByPlan.keys.toList
      .sortBy(With.prioritizer.getPriority)
      .flatten(requestsByPlan)
      .flatten(buildable => getUnfulfilledBuildables(buildable, unitsWanted, unitsActual))
    
    simulationResults = ScheduleSimulator.simulate(rawQueue)
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
      
      unitsWanted.put(unitType, request.add + unitsWanted.getOrElse(unitType, 0))
      unitsWanted.put(unitType, Math.max(request.require, unitsWanted(unitType)))
      
      if (unitsWanted(unitType) > unitsActual(unitType))
        return List(0 until unitsWanted(unitType) - unitsActual(unitType)).map(i => request.buildable)
      else
        return List.empty
    }
  }
}
