
package Planning.Plans.Army

import Lifecycle.With
import Micro.Squads.Goals.GoalAttack
import Planning.UnitCounters.UnitCountEverything
import Planning.UnitMatchers.{MatchAnd, MatchNot, MatchRecruitableForCombat, MatchWorkers}

class DoAttack extends Squadify[GoalAttack] {

  override val goal: GoalAttack = new GoalAttack
  goal.unitMatcher = MatchAnd(MatchRecruitableForCombat, MatchNot(MatchWorkers))
  goal.unitCounter = UnitCountEverything

  override def update() {
    if (With.blackboard.wantToAttack()) {
      super.update()
    }
  }
}
