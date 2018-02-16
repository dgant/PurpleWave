package Planning.Plans.Predicates.Reactive

import Lifecycle.With
import Planning.Plan
import ProxyBwapi.Races.Zerg

class EnemyMassZerglings extends Plan {
  
  description.set("Is the enemy threatening mass zerglings?")
  
  override def isComplete: Boolean = {
    With.units.enemy.count(unit => unit.is(Zerg.Zergling)) > 20
  }
}
