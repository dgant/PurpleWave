package Planning.Plans.Predicates.Reactive

import Lifecycle.With
import Planning.Plan
import ProxyBwapi.Races.Protoss

class EnemyDarkTemplarExists extends Plan {
  
  description.set("Is the enemy threatening Dark Templar?")
  
  override def isComplete: Boolean =
    With.units.enemy.exists(unit => unit.is(Protoss.DarkTemplar))
}
