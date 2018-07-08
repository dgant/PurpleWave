
package Planning.Plans.Army

import Micro.Squads.Goals.GoalAttack
import Planning.Composition.UnitCountEverything
import Planning.UnitCounters.UnitCounter
import Planning.UnitMatchers._

class Attack(
  matcher: UnitMatcher = UnitMatchAnd(UnitMatchRecruitableForCombat, UnitMatchNot(UnitMatchWorkers)),
  counter: UnitCounter = UnitCountEverything)
  extends SquadPlan[GoalAttack] {

  override val goal: GoalAttack = new GoalAttack

  override def onUpdate() {
    goal.unitMatcher = matcher
    goal.unitCounter = counter
    super.onUpdate()
  }
}
