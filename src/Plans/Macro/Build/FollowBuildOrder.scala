package Plans.Macro.Build

import Plans.Plan
import Startup.With
import Types.Buildable.Buildable
import Utilities.Caching.CacheFrame

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

class FollowBuildOrder extends Plan {
  
  description.set("Follow a build order")
  
  val _plans = new mutable.HashMap[Buildable, ListBuffer[Plan]]
  var _queue:Iterable[Buildable] = List.empty
  
  val _getChildrenCache = new CacheFrame[Iterable[Plan]](() => _getChildren)
  override def getChildren: Iterable[Plan] = _getChildrenCache.get
  def _getChildren: Iterable[Plan] = {
    val indexByBuild = new mutable.HashMap[Buildable, Int]
    _plans.keys.foreach(build => indexByBuild.put(build, 0))
    _queue.map(build => {
      val output = _plans(build)(indexByBuild(build))
      indexByBuild(build) += 1
      output
    })
  }
  
  override def onFrame() {
    //Remove complete plans
    _plans.values.foreach(plans => plans.indices.foreach(i =>
      while (i < plans.size && plans(i).isComplete) plans.remove(i)))
  
    //Add plans to match number of builds we need
    _queue = With.scheduler.queue.filter(_.frameStart <= With.frame).map(_.buildable)
    val buildsNeeded = _queue.groupBy(x => x).map(group => (group._1, group._2.size))
    buildsNeeded.keys.foreach(build => {
      if ( ! _plans.contains(build)) {
        _plans.put(build, new ListBuffer[Plan])
      }
      while (_plans(build).size < buildsNeeded(build)) {
        _plans(build).append(_buildPlan(build))
      }
      //Consider removing excess plans
    })
    
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
