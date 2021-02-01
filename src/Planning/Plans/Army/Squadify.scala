package Planning.Plans.Army

import Micro.Squads.Goals.SquadGoal
import Micro.Squads.Squad
import Planning.Prioritized

abstract class Squadify[T <: SquadGoal] extends Prioritized {
  
  val squad: Squad = new Squad
  val goal: T
  
  def update() {
    squad.setGoal(goal)
    squad.commission()
  }
}
