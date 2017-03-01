package Global.Resources

import Plans.Plan
import Startup.With
import Types.Buildable.Buildable
import bwapi.UnitType

import scala.collection.mutable

class Scheduler {
  
  val _requests = new mutable.HashMap[Plan, Iterable[Buildable]]
  val _recentlyUpdated = new mutable.HashSet[Plan]
  
  def request(requester:Plan, buildables: Iterable[Buildable]) {
    _requests.put(requester, buildables)
    _recentlyUpdated.add(requester)
  }
  
  def queue:Iterable[Buildable] = {
    val unitsWanted = new mutable.HashMap[UnitType, Int]
    val unitsActual = With.units.ours.groupBy(_.utype).mapValues(_.size)
    _requests.keys.toList
      .sortBy(With.prioritizer.getPriority)
      .flatten(_requests(_))
      .filterNot(buildable => _isFulfilled(buildable, unitsWanted, unitsActual))
  }
  
  def onFrame() {
    _requests.keySet.diff(_recentlyUpdated).foreach(_requests.remove)
    _recentlyUpdated.clear()
  }
  
  def _isFulfilled(
    buildable:Buildable,
    unitsWanted:mutable.HashMap[UnitType, Int],
    unitsActual:Map[UnitType, Int]):Boolean = {
    if (buildable.upgrade.nonEmpty) {
      return With.game.self.getUpgradeLevel(buildable.upgrade.get) >= buildable.level
    }
    if (buildable.tech.nonEmpty) {
      return With.game.self.hasResearched(buildable.tech.get)
    }
    val unitType = buildable.unit.get
    unitsWanted.put(unitType, 1 + unitsWanted.getOrElse(unitType, 0))
    return unitsActual.getOrElse(unitType, 0) >= unitsWanted(unitType)
  }
}
