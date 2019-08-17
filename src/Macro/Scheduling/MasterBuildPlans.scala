package Macro.Scheduling

import Lifecycle.With
import Macro.Buildables.Buildable
import Planning.Plan
import Planning.Plans.Macro.Build._
import Planning.Plans.Macro.BuildOrders.FollowBuildOrder

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

class MasterBuildPlans {
  
  private val maxToFollow = 200
  
  private val childrenByBuildable = new mutable.HashMap[Buildable, ListBuffer[Plan]]
  private val childrenQueue: mutable.Queue[Plan] = mutable.Queue.empty

  def getChildren: Seq[Plan] = childrenQueue

  def removeCompletedPlans(): Unit = {
    childrenByBuildable.values.foreach(plans => {
      var i = 0
      while (i < plans.size) {
        val plan = plans(i)
        if (plan.isComplete) {
          With.recruiter.release(plan)
          plans.remove(i)
        }
        else {
          i += 1
        }
      }})
  }
  def update(invoker: FollowBuildOrder): Unit = {

    childrenQueue.clear()
    removeCompletedPlans()

    // Recalculate the child plans
    val buildablesUnplanned = With.scheduler.queue.take(maxToFollow)
    val plansUnclaimed = new mutable.HashMap[Buildable, mutable.Queue[Plan]]()
    childrenByBuildable.foreach(planPair => plansUnclaimed(planPair._1) = new mutable.Queue[Plan] ++ planPair._2)

    buildablesUnplanned.foreach(buildable => {
      val existingPlans = plansUnclaimed.get(buildable)
      if (existingPlans.exists(_.nonEmpty)) {
        childrenQueue += existingPlans.get.dequeue()
      } else {
        val newPlan = makeBuildPlan(buildable)
        if ( ! childrenByBuildable.contains(buildable)) {
          childrenByBuildable(buildable) = new mutable.ListBuffer[Plan]
        }
        childrenByBuildable(buildable) += newPlan
        childrenQueue += newPlan
      }
    })
  }
  
  private def makeBuildPlan(buildable: Buildable): Plan = {
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
