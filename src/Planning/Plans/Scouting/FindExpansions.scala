package Planning.Plans.Scouting

import Lifecycle.With
import Micro.Squads.Goals.GoalFindExpansions
import Planning.Plans.Army.SquadPlan
import Planning.UnitMatchers._

class FindExpansions(matcher: UnitMatcher = UnitMatchAnd(UnitMatchRecruitableForCombat, UnitMatchNot(UnitMatchWorkers))) extends SquadPlan[GoalFindExpansions] {
  override val goal: GoalFindExpansions = new GoalFindExpansions

  override def onUpdate() {
    goal.unitMatcher = matcher
    super.onUpdate()
  }
}