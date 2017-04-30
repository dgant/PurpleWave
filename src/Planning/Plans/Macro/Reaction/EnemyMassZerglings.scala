package Planning.Plans.Macro.Reaction

import Lifecycle.With
import Planning.Plan
import ProxyBwapi.Races.Zerg

class EnemyMassZerglings extends Plan {
  
  description.set("Is the enemy threatening Mutalisks?")
  
  override def isComplete: Boolean = {
    With.units.enemy.count(unit => unit.is(Zerg.Zergling)) > 30
  }
}
