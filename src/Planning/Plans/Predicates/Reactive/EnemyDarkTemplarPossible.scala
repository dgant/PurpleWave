package Planning.Plans.Predicates.Reactive

import Lifecycle.With
import Planning.Plan
import ProxyBwapi.Races.Protoss

class EnemyDarkTemplarPossible extends Plan {
  
  description.set("Is the enemy threatening Dark Templar?")
  
  override def isComplete: Boolean = {
    With.units.enemy.exists(unit =>
      unit.is(Protoss.DarkTemplar)      ||
      unit.is(Protoss.HighTemplar)      ||
      unit.is(Protoss.Archon)           ||
      unit.is(Protoss.DarkArchon)       ||
      unit.is(Protoss.CitadelOfAdun)    ||
      unit.is(Protoss.TemplarArchives))
  }
}
