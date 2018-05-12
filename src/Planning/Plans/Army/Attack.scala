
package Planning.Plans.Army

import Lifecycle.With
import Micro.Squads.Goals.GoalAttack
import Micro.Squads.Squad
import Planning.Composition.UnitCountEverything
import Planning.Composition.UnitCounters.UnitCounter
import Planning.Composition.UnitMatchers.{UnitMatchWarriors, UnitMatcher}
import Planning.Plan

class Attack(
  attackerMatcher: UnitMatcher = UnitMatchWarriors,
  attackerCounter: UnitCounter = UnitCountEverything) extends Plan {
  
  val squad = new Squad(this)
  val goal = new GoalAttack
  
  override def onUpdate() {
    goal.unitMatcher = attackerMatcher
    goal.unitCounter = attackerCounter
    squad.setGoal(goal)
    With.squads.commission(squad)
  }
}
