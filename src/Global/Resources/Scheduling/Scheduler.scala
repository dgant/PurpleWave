package Global.Resources.Scheduling

import Plans.Plan
import Startup.With
import Types.Buildable.Buildable
import Utilities.Caching.Limiter
import bwapi.UnitType

import scala.collection.mutable

class Scheduler {
  
  val _requests = new mutable.HashMap[Plan, Iterable[Buildable]]
  val _recentlyUpdated = new mutable.HashSet[Plan]
  var simulationResults:ScheduleSimulationResult = new ScheduleSimulationResult(List.empty, List.empty, List.empty)
  
  def queue:Iterable[BuildEvent] = simulationResults.suggestedEvents
  
  def request(requester:Plan, buildables: Iterable[Buildable]) {
    _requests.put(requester, buildables)
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
  
    simulationResults = ScheduleSimulator.simulate(rawQueue)
  }
  
  
  
  def _isFulfilled(
    buildable:Buildable,
    unitsWanted:mutable.HashMap[UnitType, Int],
    unitsActual:Map[UnitType, Int]):Boolean = {
    if (buildable.upgradeOption.nonEmpty) {
      return With.self .getUpgradeLevel(buildable.upgradeOption.get) >= buildable.upgradeLevel
    }
    else if (buildable.techOption.nonEmpty) {
      return With.self.hasResearched(buildable.techOption.get)
    }
    else {
      val unitType = buildable.unitOption.get
      unitsWanted.put(unitType, 1 + unitsWanted.getOrElse(unitType, 0))
      unitsActual.getOrElse(unitType, 0) >= unitsWanted(unitType)
    }
  }
}
