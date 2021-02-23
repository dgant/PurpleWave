package Planning.Tactics

import Lifecycle.With
import Micro.Squads.Goals.GoalCatchDTRunby
import Planning.UnitCounters.CountOne
import Planning.UnitMatchers._
import ProxyBwapi.Races.Protoss

class CatchDTRunby extends Squadify[GoalCatchDTRunby] {

  override val goal: GoalCatchDTRunby = new GoalCatchDTRunby
  goal.unitMatcher = MatchMobileDetector
  goal.unitCounter = CountOne

  override def update() {
    if (With.enemies.map(With.unitsShown(_, Protoss.DarkTemplar)).sum == 0) return
    super.update()
  }
}
