package Planning.Plans.Scouting

import Micro.Squads.Goals.GoalCampExpansions
import Planning.Plans.Army.SquadPlan

class CampExpansions extends SquadPlan[GoalCampExpansions] {
  override val goal: GoalCampExpansions = new GoalCampExpansions
}