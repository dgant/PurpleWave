
package Planning.Plans.Army

import Micro.Squads.Goals.GoalAttack
import Planning.Composition.UnitCountEverything
import Planning.Composition.UnitCounters.UnitCounter
import Planning.Composition.UnitMatchers._

class Attack(
  attackerMatcher: UnitMatcher = UnitMatchAnd(UnitMatchRecruitableForCombat, UnitMatchNot(UnitMatchWorkers)),
  attackerCounter: UnitCounter = UnitCountEverything)
  extends SquadPlan[GoalAttack] {
  
  override val goal: GoalAttack = new GoalAttack
  
  override def onUpdate() {
    goal.unitMatcher = attackerMatcher
    goal.unitCounter = attackerCounter
    super.onUpdate()
  }
}
