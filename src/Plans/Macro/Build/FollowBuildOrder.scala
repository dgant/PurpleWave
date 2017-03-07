package Plans.Macro.Build

import Plans.Plan
import Startup.With
import Types.Buildable.Buildable

import scala.collection.mutable

class FollowBuildOrder extends Plan {
  
  description.set("Follow a build order")
  
  var _queue:Iterable[Buildable] = List.empty
  val _plans = new mutable.HashMap[Buildable, Plan]
  
  override def getChildren: Iterable[Plan] = _queue.flatMap(_plans.get)
  
  override def onFrame() {
    _plans.filter(_._2.isComplete).keys.foreach(_plans.remove)
    _queue = With.scheduler.queue
  
    val buildsThatNeedPlans = _queue.filterNot(_plans.contains)
    buildsThatNeedPlans.foreach(order => _plans(order) = _buildPlan(order))
    getChildren.foreach(_.onFrame())
  }
  
  def _buildPlan(buildable:Buildable):Plan = {
    if (buildable.unit.nonEmpty) {
      val unitType = buildable.unit.get
      if (unitType.isBuilding) {
        return new BuildBuilding(unitType)
      } else {
        return new TrainUnit(unitType)
      }
    }
    if (buildable.tech.nonEmpty) {
      return new ResearchTech(buildable.tech.get)
    }
    if (buildable.upgrade.nonEmpty) {
      return new ResearchUpgrade(buildable.upgrade.get, buildable.level)
    }
    
    throw new Exception("Tried to build a Buildable that doesn't specify any unit, tech, or research")
  }
}
