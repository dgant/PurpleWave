package Macro.Scheduling

import Lifecycle.With
import Macro.Buildables.Buildable
import Mathematics.Maff
import Planning.Plans.Macro.Build._
import Tactics.FollowBuildOrder

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

class MasterBuildPlans {

  private val maxToFollow = 200
  private val plans = new mutable.HashMap[Buildable, ListBuffer[Production]]

  def getChildren: Iterable[Production] = buildChildren
  private var buildChildren: Iterable[Production] = Iterable.empty

  def update(invoker: FollowBuildOrder) {

    // Add plans to match number of builds we need
    val queue = With.scheduler.queue.take(maxToFollow)

    val buildsNeeded =
      queue
        .groupBy(buildable => buildable)
        .map(buildable => (
          buildable._1,
          buildable._2.size))

    // Add needed builds
    buildsNeeded.keys.foreach(build => {
      if ( ! plans.contains(build)) {
        plans.put(build, new ListBuffer[Production])
      }
      while (plans(build).size < buildsNeeded(build)) {
        plans(build).append(buildPlan(build))
      }
    })

    // Remove complete plans
    plans.values.foreach(plans => {
      var i = 0
      while (i < plans.size) {
        val plan = plans(i)
        if (plan.isComplete) {
          plan.onCompletion()
          With.recruiter.release(plan)
          plans.remove(i)
        }
        else {
          i += 1
        }
      }})

    // Remove unneeded builds
    plans.keys.foreach(build => {
      // TODO: This can break really badly!
      while (plans(build).size > buildsNeeded.getOrElse(build, 0)) {
        val removablePlan = Maff.maxBy(plans(build).filterNot(With.bank.hasSpentRequest))(_.priorityUntouched)
        removablePlan.foreach(plans(build).-=)
      }
    })

    // Recalculate children
    val indexByBuild = new mutable.HashMap[Buildable, Int]
    plans.keys.foreach(build => indexByBuild.put(build, 0))
    val childrenMappedFromQueue = queue.map(build => {
      val output = plans.get(build).flatMap(_.lift(indexByBuild(build)))
      indexByBuild(build) += 1
      output
    }).filter(_.nonEmpty).map(_.get)
    val childrenUnmappedFromQueue = plans.flatMap(_._2).filterNot(childrenMappedFromQueue.contains)
    buildChildren = (
      childrenUnmappedFromQueue.filter(With.bank.hasSpentRequest).toVector.sortBy(_.frameCreated)
      ++ childrenMappedFromQueue
      ++ childrenUnmappedFromQueue.filterNot(With.bank.hasSpentRequest).toVector.sortBy(_.frameCreated))
  }

  private def buildPlan(buildable: Buildable): Production = {
    if (buildable.unitOption.nonEmpty) {
      val unitClass = buildable.unitOption.get
      if (unitClass.isAddon) {
        return new BuildAddon(unitClass)
      } else if (unitClass.isBuilding && ! unitClass.whatBuilds._1.isBuilding) {
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