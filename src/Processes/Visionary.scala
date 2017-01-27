package Processes

import Types.Plans.{Plan, PlanGatherMinerals}
import scala.collection.mutable.ListBuffer

class Visionary {

  val _defaultPlans = ListBuffer[Plan]()
  _defaultPlans :+ new PlanGatherMinerals()

  def envisionPlans(): Seq[Plan] = {
    return _defaultPlans
  }
}