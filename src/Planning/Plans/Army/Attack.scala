
package Planning.Plans.Army

import Lifecycle.With
import Micro.Squads.Goals.GoalAttack
import Planning.UnitCounters.{UnitCountEverything, UnitCounter}
import Planning.UnitMatchers._
import ProxyBwapi.UnitClasses.UnitClass

class Attack(
  matcher: UnitMatcher = UnitMatchAnd(UnitMatchRecruitableForCombat, UnitMatchNot(UnitMatchWorkers)),
  counter: UnitCounter = UnitCountEverything)
  extends SquadPlan[GoalAttack] {

  override val goal: GoalAttack = new GoalAttack

  override def onUpdate() {
    goal.unitMatcher = matcher
    goal.unitCounter = counter
    if (With.units.ours.exists(matcher.accept)) {
      With.blackboard.wantToAttack.set(true)
    }
    super.onUpdate()
  }

  description.set(super.toString + (if (matcher.isInstanceOf[UnitClass]) "(" + matcher + ")" else ""))
}
