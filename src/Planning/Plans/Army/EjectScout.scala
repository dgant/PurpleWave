package Planning.Plans.Army

import Lifecycle.With
import Micro.Squads.Goals.GoalEjectScout
import Planning.Plans.Scouting.ScoutCleared
import Planning.UnitCounters.CountOne
import Planning.UnitMatchers._
import Utilities.Minutes

class EjectScout extends Squadify[GoalEjectScout] {

  override val goal: GoalEjectScout = new GoalEjectScout
  private val scoutCleared = new ScoutCleared

  override def update() {
    if (With.frame > Minutes(8)()) return
    if (scoutCleared.isComplete) return

    val scouts = With.scouting.enemyScouts()
    if (scouts.isEmpty) return

    goal.unitMatcher = MatchAnd(MatchScoutCatcher, (unit) => unit.base.exists(With.scouting.basesToLookForEnemyScouts().contains) || unit.pixelsToGetInRange(goal.targetScout().get) < 32)
    goal.unitCounter = CountOne
    super.update()
  }
}
