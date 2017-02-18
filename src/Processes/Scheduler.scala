package Processes

import Plans.Plan
import Startup.With
import Types.Buildable.Buildable

import scala.collection.mutable

class Scheduler {
  
  val _requests = new mutable.HashMap[Plan, Iterable[Buildable]]
  val _recentlyUpdated = new mutable.HashSet[Plan]
  
  def request(requester:Plan, buildables: Iterable[Buildable]) {
    _requests.put(requester, buildables)
    _recentlyUpdated.add(requester)
  }
  
  def queue:Iterable[Buildable] = {
    _requests.keys.toList.sortBy(With.prioritizer.getPriority).flatten(_requests(_))
  }
  
  def onFrame() {
    _requests.keySet.diff(_recentlyUpdated).foreach(_requests.remove)
    _recentlyUpdated.clear()
  }
}
