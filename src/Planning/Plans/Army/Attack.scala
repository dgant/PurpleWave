
package Planning.Plans.Army

import Lifecycle.With
import Micro.Squads.Goals.GoalAttack
import Planning.UnitCounters.{UnitCountEverything, UnitCounter}
import Planning.UnitMatchers._

class Attack(
  matcher: UnitMatcher = UnitMatchAnd(UnitMatchRecruitableForCombat, UnitMatchNot(UnitMatchWorkers)),
  counter: UnitCounter = UnitCountEverything)
  extends SquadPlan[GoalAttack] {

  override val goal: GoalAttack = new GoalAttack

  override def onUpdate() {
    if (squad.previousUnits.nonEmpty) {
      With.blackboard.wantToAttack.set(true)
    }
    goal.unitMatcher = matcher
    goal.unitCounter = counter
    super.onUpdate()
  }

  description.set(super.toString + (if (matcher != UnitMatchAnd(UnitMatchRecruitableForCombat, UnitMatchNot(UnitMatchWorkers))) "(" + matcher + ")" else ""))
}
