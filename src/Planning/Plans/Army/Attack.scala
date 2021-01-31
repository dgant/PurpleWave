
package Planning.Plans.Army

import Lifecycle.With
import Micro.Squads.Goals.GoalAttack
import Planning.UnitCounters.UnitCountEverything
import Planning.UnitMatchers._

class Attack extends SquadPlan[GoalAttack] {

  override val goal: GoalAttack = new GoalAttack

  override def onUpdate() {
    goal.unitMatcher = UnitMatchAnd(UnitMatchRecruitableForCombat, UnitMatchNot(UnitMatchWorkers))
    goal.unitCounter = UnitCountEverything
    With.blackboard.wantToAttack.set(true)
    super.onUpdate()
  }
}
