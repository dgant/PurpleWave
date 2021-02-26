package Tactics

import Lifecycle.With
import Micro.Squads.Goals.GoalCatchDTRunby
import ProxyBwapi.Races.Protoss

class CatchDTRunby extends Squadify[GoalCatchDTRunby] {

  override val goal: GoalCatchDTRunby = new GoalCatchDTRunby

  override def update() {
    if (With.enemies.map(With.unitsShown(_, Protoss.DarkTemplar)).sum == 0) return
    super.update()
  }
}
