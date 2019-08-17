package Planning.Plans.Scouting

import Micro.Squads.Goals.GoalCampExpansions
import Planning.Plans.Army.SquadPlan
import Planning.UnitMatchers.UnitMatcher

class CampExpansions(unitMatcher: Option[UnitMatcher]) extends SquadPlan[GoalCampExpansions] {
  def this(unitMatcher: UnitMatcher) {
    this(Some(unitMatcher))
  }

  override val goal: GoalCampExpansions = new GoalCampExpansions
  unitMatcher.foreach(goal.unitMatcher = _)
}