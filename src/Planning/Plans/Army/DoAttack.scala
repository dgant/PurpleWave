
package Planning.Plans.Army

import Lifecycle.With
import Micro.Squads.Goals.GoalAttack
import Planning.UnitCounters.UnitCountEverything
import Planning.UnitMatchers.{UnitMatchAnd, UnitMatchNot, UnitMatchRecruitableForCombat, UnitMatchWorkers}

class DoAttack extends SquadPlan[GoalAttack] {

  override val goal: GoalAttack = new GoalAttack
  goal.unitMatcher = UnitMatchAnd(UnitMatchRecruitableForCombat, UnitMatchNot(UnitMatchWorkers))
  goal.unitCounter = UnitCountEverything

  override def onUpdate() {
    if (With.blackboard.wantToAttack()) {
      super.onUpdate()
    }
  }
}
