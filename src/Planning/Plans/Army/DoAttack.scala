
package Planning.Plans.Army

import Lifecycle.With
import Micro.Squads.Goals.GoalAttack
import Planning.UnitCounters.UnitCountEverything
import Planning.UnitMatchers.{UnitMatchAnd, UnitMatchNot, UnitMatchRecruitableForCombat, UnitMatchWorkers}

class DoAttack extends Squadify[GoalAttack] {

  override val goal: GoalAttack = new GoalAttack
  goal.unitMatcher = UnitMatchAnd(UnitMatchRecruitableForCombat, UnitMatchNot(UnitMatchWorkers))
  goal.unitCounter = UnitCountEverything

  override def update() {
    if (With.blackboard.wantToAttack()) {
      super.update()
    }
  }
}
