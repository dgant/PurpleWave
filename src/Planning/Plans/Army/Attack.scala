
package Planning.Plans.Army

import Micro.Squads.Goals.GoalAttack
import Planning.Composition.UnitCountEverything
import Planning.Composition.UnitCounters.UnitCounter
import Planning.Composition.UnitMatchers.{UnitMatchWarriors, UnitMatcher}

class Attack(
  attackerMatcher: UnitMatcher = UnitMatchWarriors,
  attackerCounter: UnitCounter = UnitCountEverything)
  extends BasicSquad[GoalAttack] {
  
  override def onUpdate() {
    goal.unitMatcher = attackerMatcher
    goal.unitCounter = attackerCounter
    super.onUpdate()
  }
}
