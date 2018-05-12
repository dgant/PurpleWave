package Planning.Plans.Army

import Micro.Squads.Goals.SquadGoal
import Micro.Squads.Squad
import Planning.Plan

class BasicSquad[T <: SquadGoal](implicit t: Manifest[T]) extends Plan {
  
  val squad: Squad = new Squad(this)
  val goal: T = t.runtimeClass.newInstance.asInstanceOf[T]
  
  override def onUpdate() {
    squad.setGoal(goal)
    squad.commission()
  }
}
