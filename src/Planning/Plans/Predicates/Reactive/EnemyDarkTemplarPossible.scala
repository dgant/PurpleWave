package Planning.Plans.Predicates.Reactive

import Lifecycle.With
import Planning.Plan
import ProxyBwapi.Races.Protoss

class EnemyDarkTemplarPossible extends Plan {
  
  override def isComplete: Boolean = {
    With.units.enemy.exists(_.isAny(
      Protoss.DarkTemplar,
      Protoss.HighTemplar,
      Protoss.Archon,
      Protoss.DarkArchon,
      Protoss.CitadelOfAdun,
      Protoss.TemplarArchives))
  }
}
