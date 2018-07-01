package Macro.Scheduling

import Lifecycle.With
import Macro.Buildables.Buildable
import Performance.Cache
import Planning.Plan
import Planning.Plans.Macro.Build._
import Planning.Plans.Macro.BuildOrders.FollowBuildOrder
import ProxyBwapi.UnitClasses.UnitClass

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

class MasterBuildPlans {
  
  private val maxToFollow = 200
  
  private val plans = new mutable.HashMap[Buildable, ListBuffer[Plan]]
  private var queue: Iterable[Buildable] = Vector.empty
  
  def getChildren: Iterable[Plan] = getChildrenCache()
  private val getChildrenCache = new Cache(() => getChildrenRecalculate)
  private def getChildrenRecalculate: Iterable[Plan] = {
    val indexByBuild = new mutable.HashMap[Buildable, Int]
    plans.keys.foreach(build => indexByBuild.put(build, 0))
    queue.map(build => {
      val output = plans(build)(indexByBuild(build))
      indexByBuild(build) += 1
      output
    })
  }
  
  def update(invoker: FollowBuildOrder) {

    //Remove complete plans
    plans.values.foreach(plans => {
      var i = 0
      while (i < plans.size) {
        val plan = plans(i)
        if (plan.isComplete) {
          With.bank.release(plan)
          With.recruiter.release(plan)
          plans.remove(i)
        }
        else {
          i += 1
        }
      }})

    //Add plans to match number of builds we need
    queue = With.scheduler.queue.take(maxToFollow)

    val buildsNeeded =
      queue
        .groupBy(buildable => buildable)
        .map(buildable => (
          buildable._1,
          buildable._2.size))

    buildsNeeded.keys.foreach(build => {
      if ( ! plans.contains(build)) {
        plans.put(build, new ListBuffer[Plan])
      }
      while (plans(build).size < buildsNeeded(build)) {
        plans(build).append(buildPlan(build))
      }

      //Consider removing excess plans
    })

    getChildrenCache.invalidate()
  }
  
  private def buildPlan(buildable: Buildable): Plan = {
    if (buildable.unitOption.nonEmpty) {
      val unitClass = buildable.unitOption.get
      if (unitClass.isAddon) {
        return new BuildAddon(unitClass)
      }
      else if (unitClass.isBuilding && ! unitClass.whatBuilds._1.isBuilding) {
        return new BuildBuilding(unitClass)
      } else if (unitClass.buildUnitsSpent.exists(_.isZerg)) {
        return new MorphUnit(unitClass)
      } else {
        return new TrainUnit(unitClass)
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
