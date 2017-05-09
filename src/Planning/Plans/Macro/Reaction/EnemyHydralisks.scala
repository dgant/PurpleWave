package Planning.Plans.Macro.Reaction

import Lifecycle.With
import Planning.Plan
import ProxyBwapi.Races.Zerg

class EnemyHydralisks extends Plan {
  
  description.set("Is the enemy threatening Hydralisks?")
  
  override def isComplete: Boolean = {
    With.units.enemy.exists(unit => unit.is(Zerg.Hydralisk) || unit.is(Zerg.HydraliskDen))
  }
}
