package Planning.Plans.Scouting

import Lifecycle.With
import Micro.Squads.Goals.GoalFindExpansions
import Micro.Squads.Squad
import Planning.Plan

class FindExpansions extends Plan {
  
  val squad = new Squad(this)
  val goal = new GoalFindExpansions
  
  override def onUpdate() {
    squad.setGoal(goal)
    With.squads.commission(squad)
  }
}
