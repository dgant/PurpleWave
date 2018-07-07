package Planning.Plans.GamePlans.Protoss.Situational

import Lifecycle.With
import Micro.Squads.Goals.GoalCatchDTRunby
import Planning.Plans.Army.SquadPlan
import Planning.UnitCounters.{UnitCountOne, UnitCounter}
import Planning.UnitMatchers._
import ProxyBwapi.Races.Protoss

class CatchDTRunby(
  attackerMatcher: UnitMatcher = UnitMatchMobileDetectors,
  attackerCounter: UnitCounter = UnitCountOne)
  extends SquadPlan[GoalCatchDTRunby] {
  
  override val goal: GoalCatchDTRunby = new GoalCatchDTRunby
  
  override def onUpdate() {
    if (With.enemies.map(With.intelligence.unitsShown(_, Protoss.DarkTemplar)).sum == 0) {
      return
    }

    goal.unitMatcher = attackerMatcher
    goal.unitCounter = attackerCounter
    super.onUpdate()
  }
}
