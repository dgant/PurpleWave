
package Planning.Plans.Army

import Lifecycle.With
import Micro.Squads.Goals.GoalAttack
import Planning.ResourceLocks.LockUnits
import Planning.UnitCounters.{UnitCountEverything, UnitCounter}
import Planning.UnitMatchers._
import ProxyBwapi.UnitClasses.UnitClass

class Attack(
    matcherArgument: UnitMatcher = UnitMatchNone,
    counter: UnitCounter = UnitCountEverything)
  extends SquadPlan[GoalAttack] {

  override val goal: GoalAttack = new GoalAttack

  val conscript = matcherArgument != UnitMatchNone
  val matcher = if (matcherArgument == UnitMatchNone)
    UnitMatchAnd(UnitMatchRecruitableForCombat, UnitMatchNot(UnitMatchWorkers))
  else
    UnitMatchAnd(UnitMatchComplete, matcherArgument)

  private val attackers: LockUnits = new LockUnits{
    unitMatcher.set(matcher)
    unitCounter.set(counter)
  }

  override def onUpdate() {
    if ( ! With.units.ours.exists(_.is(matcher))) // Performance short-circuit
    if (conscript) {
      attackers.acquire(this)
      squad.addConscripts(attackers.units)
    }
    goal.unitMatcher = attackers.unitMatcher.get
    goal.unitCounter = attackers.unitCounter.get
    if (With.units.ours.exists(matcher.apply)) {
      With.blackboard.wantToAttack.set(true)
    }
    super.onUpdate()
  }

  description.set(super.toString + (if (matcherArgument.isInstanceOf[UnitClass]) "(" + matcherArgument + ")" else ""))
}
