package Planning.Plans.GamePlans.Protoss.Situational

import Lifecycle.With
import Micro.Squads.Goals.GoalCatchDTRunby
import Planning.Plans.Army.Squadify
import Planning.UnitCounters.UnitCountOne
import Planning.UnitMatchers._
import ProxyBwapi.Races.Protoss

class CatchDTRunby extends Squadify[GoalCatchDTRunby] {

  override val goal: GoalCatchDTRunby = new GoalCatchDTRunby
  goal.unitMatcher = MatchMobileDetector
  goal.unitCounter = UnitCountOne

  override def update() {
    if (With.enemies.map(With.unitsShown(_, Protoss.DarkTemplar)).sum == 0) return
    super.update()
  }
}
