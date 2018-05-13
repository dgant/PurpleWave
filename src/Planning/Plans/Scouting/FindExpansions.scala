package Planning.Plans.Scouting

import Micro.Squads.Goals.GoalFindExpansions
import Planning.Plans.Army.SquadPlan

class FindExpansions extends SquadPlan[GoalFindExpansions] {
  override val goal: GoalFindExpansions = new GoalFindExpansions
}