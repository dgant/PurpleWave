package Planning.Plans.Macro.Reaction

import Lifecycle.With
import Planning.Plan
import ProxyBwapi.Races.Zerg

class EnemyLurkers extends Plan {
  
  description.set("Is the enemy threatening Dark Templar?")
  
  override def isComplete: Boolean = {
    With.units.enemy.exists(_.is(Zerg.Lurker)) ||
    (
      (
        With.units.enemy.exists(_.is(Zerg.Hydralisk)) ||
        With.units.enemy.exists(_.is(Zerg.HydraliskDen))
      )
      && With.units.enemy.exists(_.is(Zerg.HydraliskDen))
    )
  }
}
