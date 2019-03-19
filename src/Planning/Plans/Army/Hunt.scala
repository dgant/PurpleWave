
package Planning.Plans.Army

import Lifecycle.With
import Micro.Squads.Goals.GoalHunt
import Planning.UnitCounters.{UnitCountEverything, UnitCounter}
import Planning.UnitMatchers._

class Hunt(
  hunterMatcher: UnitMatcher,
  enemyMatcher: UnitMatcher,
  hunterCounter: UnitCounter =  UnitCountEverything)
  extends SquadPlan[GoalHunt] {

  override val goal: GoalHunt = new GoalHunt(enemyMatcher)

  override def onUpdate() {
    if (With.units.enemy.exists(u => u.is(enemyMatcher) && u.possiblyStillThere)) {
      goal.unitMatcher = hunterMatcher
      goal.unitCounter = hunterCounter
      super.onUpdate()
    }
  }
}
