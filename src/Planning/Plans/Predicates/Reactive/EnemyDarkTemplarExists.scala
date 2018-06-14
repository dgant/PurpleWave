package Planning.Plans.Predicates.Reactive

import Lifecycle.With
import Planning.Predicate
import ProxyBwapi.Races.Protoss

class EnemyDarkTemplarExists extends Predicate {
  
  override def isComplete: Boolean =
    With.units.enemy.exists(unit => unit.is(Protoss.DarkTemplar))
}
