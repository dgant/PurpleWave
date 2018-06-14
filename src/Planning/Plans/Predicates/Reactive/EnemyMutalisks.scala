package Planning.Plans.Predicates.Reactive

import Lifecycle.With
import Planning.Predicate
import ProxyBwapi.Races.Zerg

class EnemyMutalisks extends Predicate {
  
  description.set("Is the enemy threatening Mutalisks?")
  
  override def isComplete: Boolean = {
    With.units.enemy.exists(unit => unit.is(Zerg.Mutalisk))
  }
}
