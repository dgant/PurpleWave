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
    if (buildable.unitOption.nonEmpty) {
      val unitType = buildable.unitOption.get
      if (unitType.isBuilding) {
        return new BuildBuilding(unitType)
      } else {
        return new TrainUnit(unitType)
      }
    }
    if (buildable.techOption.nonEmpty) {
      return new ResearchTech(buildable.techOption.get)
    }
    if (buildable.upgradeOption.nonEmpty) {
      return new ResearchUpgrade(buildable.upgradeOption.get, buildable.upgradeLevel)
    }
    
    throw new Exception("Tried to build a Buildable that doesn't specify any unit, tech, or research")
  }
}
