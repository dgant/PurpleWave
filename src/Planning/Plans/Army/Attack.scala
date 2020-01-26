
package Planning.Plans.Army

import Lifecycle.With
import Micro.Squads.Goals.GoalAttack
import Planning.ResourceLocks.LockUnits
import Planning.UnitCounters.{UnitCountEverything, UnitCounter}
import Planning.UnitMatchers._
import ProxyBwapi.UnitClasses.UnitClass

class Attack(
  matcher: UnitMatcher = UnitMatchNone,
  counter: UnitCounter = UnitCountEverything)
  extends SquadPlan[GoalAttack] {

  override val goal: GoalAttack = new GoalAttack

  val forcedAttackers: LockUnits = new LockUnits{
    unitMatcher.set(matcher)
    unitCounter.set(counter)
  }

  override def onUpdate() {
    var actualMatcher: UnitMatcher = matcher
    if (actualMatcher == UnitMatchNone) {
      actualMatcher = UnitMatchAnd(UnitMatchRecruitableForCombat, UnitMatchNot(UnitMatchWorkers))
    } else {
      forcedAttackers.acquire(this)
      squad.addConscripts(forcedAttackers.units)
    }
    goal.unitMatcher = actualMatcher
    goal.unitCounter = counter
    if (With.units.ours.exists(actualMatcher.accept)) {
      With.blackboard.wantToAttack.set(true)
    }
    super.onUpdate()
  }

  description.set(super.toString + (if (matcher.isInstanceOf[UnitClass]) "(" + matcher + ")" else ""))
}
