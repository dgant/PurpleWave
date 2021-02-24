
package Tactics

import Lifecycle.With
import Micro.Squads.Goals.GoalAttack
import Planning.UnitCounters.CountEverything
import Planning.UnitMatchers.{MatchAnd, MatchNot, MatchRecruitableForCombat, MatchWorkers}

class DoAttack extends Squadify[GoalAttack] {

  override val goal: GoalAttack = new GoalAttack
  goal.unitMatcher = MatchAnd(MatchRecruitableForCombat, MatchNot(MatchWorkers))
  goal.unitCounter = CountEverything

  override def update() {
    if (With.blackboard.wantToAttack()) {
      super.update()
    }
  }
}
