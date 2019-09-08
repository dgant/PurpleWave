package Macro.Scheduling

import Lifecycle.With
import Macro.Buildables.Buildable
import Planning.Plan
import Planning.Plans.Macro.Build._
import Planning.Plans.Macro.BuildOrders.FollowBuildOrder

import scala.collection.mutable

class MasterBuildPlans {
  
  private val maxToFollow = 200

  private var childrenPlans: mutable.ArrayBuffer[ProductionPlan] = mutable.ArrayBuffer.empty
  
  def getChildren: Seq[Plan] = childrenPlans

  var lastUpdateFrame: Int = -1
  def update(invoker: FollowBuildOrder): Unit = {
    if (With.frame == lastUpdateFrame) return
    lastUpdateFrame = With.frame

    // 1. Release completed plans
    // 2. Map current buildables to available plans; produce new plans as needed
    // 3. Replace the queue

    val newPlans = new mutable.ArrayBuffer[ProductionPlan](childrenPlans.size)
    val livingPlans = new mutable.ListBuffer[ProductionPlan]
    var i = 0
    childrenPlans.foreach(plan => {
      if (plan.isComplete) {
        With.recruiter.release(plan)
      }
      else {
        livingPlans += plan
      }
    })

    With.scheduler.queue.take(maxToFollow).foreach(buildable => {
      var plan: Option[ProductionPlan] = None
      i = 0
      while (i < livingPlans.length) {
        if (livingPlans(i).buildable == buildable) {
          plan = Some(livingPlans.remove(i))
          i = livingPlans.length
        }
        i += 1
      }
      plan = plan.orElse(Some(makeBuildPlan(buildable)))
      newPlans += plan.get
    })
    childrenPlans = newPlans
  }
  
  private def makeBuildPlan(buildable: Buildable): ProductionPlan = {
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
