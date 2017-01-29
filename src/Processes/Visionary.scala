package Processes

import Types.Plans.{Plan, PlanFollowBuildOrder, PlanGatherMinerals}

import scala.collection.mutable.ListBuffer

class Visionary {
  val _defaultPlans:ListBuffer[Plan] = ListBuffer.empty

  def envisionPlans(): Seq[Plan] = {
    if (_defaultPlans.length == 0) {
      _populateDefaultPlans
    }

    _defaultPlans
  }

  def _populateDefaultPlans(): Unit = {
    _defaultPlans += new PlanGatherMinerals
    _defaultPlans += new PlanFollowBuildOrder
  }
}