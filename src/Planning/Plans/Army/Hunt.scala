
package Planning.Plans.Army

import Lifecycle.With
import Micro.Squads.Goals.GoalHunt
import Planning.UnitMatchers._

class Hunt(
  hunterMatcher: UnitMatcher,
  enemyMatcher: UnitMatcher)
  extends SquadPlan[GoalHunt] {

  override val goal: GoalHunt = new GoalHunt(enemyMatcher)

  override def onUpdate() {
    if (With.units.enemy.exists(u => u.is(enemyMatcher) && u.possiblyStillThere)) {
      goal.unitMatcher = hunterMatcher
      super.onUpdate()
    }
  }
}
