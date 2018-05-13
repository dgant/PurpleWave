package Planning.Plans.Army

import Micro.Squads.Goals.SquadGoal
import Micro.Squads.Squad
import Planning.Plan

abstract class SquadPlan[T <: SquadGoal] extends Plan {
  
  val squad: Squad = new Squad(this)
  val goal: T
  
  override def onUpdate() {
    squad.setGoal(goal)
    squad.commission()
  }
}
