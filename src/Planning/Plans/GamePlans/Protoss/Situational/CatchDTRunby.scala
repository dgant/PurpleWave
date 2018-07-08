package Planning.Plans.GamePlans.Protoss.Situational

import Lifecycle.With
import Micro.Squads.Goals.GoalCatchDTRunby
import Planning.Plans.Army.SquadPlan
import Planning.UnitCounters.{UnitCountOne, UnitCounter}
import Planning.UnitMatchers._
import ProxyBwapi.Races.Protoss

class CatchDTRunby(
  matcher: UnitMatcher = UnitMatchMobileDetectors,
  counter: UnitCounter = UnitCountOne)
  extends SquadPlan[GoalCatchDTRunby] {

  override val goal: GoalCatchDTRunby = new GoalCatchDTRunby

  override def onUpdate() {
    if (With.enemies.map(With.intelligence.unitsShown(_, Protoss.DarkTemplar)).sum == 0) {
      return
    }

    goal.unitMatcher = matcher
    goal.unitCounter = counter
    super.onUpdate()
  }
}
