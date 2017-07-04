package Planning.Plans.Information.Scenarios

import Lifecycle.With
import Planning.Plan
import ProxyBwapi.Races.Zerg

class WeAreBeing4Pooled extends Plan {
  
  override def isComplete: Boolean = {
    With.frame < 24 * 60 * 4 &&
    (
      // 5 pool spawning pool finishes about 1:35 == 95 seconds
      // Let's add a bit of buffer to our detection.
      
      With.units.enemy.exists(_.is(Zerg.Zergling)) ||
      With.units.enemy.exists(u =>
        u.is(Zerg.SpawningPool) &&
        (u.complete || With.frame + u.remainingBuildFrames < 24 * 120))
    )
  }
}
