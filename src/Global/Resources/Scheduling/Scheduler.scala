package Global.Resources.Scheduling

import Plans.Plan
import Startup.With
import Types.BuildRequest.BuildRequest
import Utilities.Caching.Limiter
import bwapi.UnitType

import scala.collection.mutable

class Scheduler {
  
  val _requests = new mutable.HashMap[Plan, Iterable[BuildRequest]]
  val _recentlyUpdated = new mutable.HashSet[Plan]
  var simulationResults:ScheduleSimulationResult = new ScheduleSimulationResult(List.empty, List.empty, List.empty)
  
  def queue:Iterable[BuildEvent] = simulationResults.suggestedEvents
  
  def request(requester:Plan, requests: Iterable[BuildRequest]) {
    _requests.put(requester, requests)
    _recentlyUpdated.add(requester)
  }
  
  def onFrame() = {
    _requests.keySet.diff(_recentlyUpdated).foreach(_requests.remove)
    _recentlyUpdated.clear()
    _updateQueueLimiter.act()
  }
  val _updateQueueLimiter = new Limiter(2, () => _updateQueue)
  def _updateQueue() {
    val unitsWanted = new mutable.HashMap[UnitType, Int]
    val unitsActual = With.units.ours.groupBy(_.utype).mapValues(_.size)
    val rawQueue = _requests.keys.toList
      .sortBy(With.prioritizer.getPriority)
      .flatten(_requests)
      .filterNot(buildable => _isFulfilled(buildable, unitsWanted, unitsActual))
      .map(_.buildable)
  
    simulationResults = ScheduleSimulator.simulate(rawQueue)
  }
  
  def _isFulfilled(
    request:BuildRequest,
    unitsWanted:mutable.HashMap[UnitType, Int],
    unitsActual:Map[UnitType, Int]):Boolean = {
    if (request.buildable.upgradeOption.nonEmpty) {
      return With.self .getUpgradeLevel(request.buildable.upgradeOption.get) >= request.buildable.upgradeLevel
    }
    else if (request.buildable.techOption.nonEmpty) {
      return With.self.hasResearched(request.buildable.techOption.get)
    }
    else {
      val unitType = request.buildable.unitOption.get
      
      unitsWanted.put(unitType, request.add + unitsWanted.getOrElse(unitType, 0))
      unitsWanted.put(unitType, Math.max(request.require, unitsWanted(unitType)))
      unitsActual.getOrElse(unitType, 0) >= unitsWanted(unitType)
    }
  }
}
