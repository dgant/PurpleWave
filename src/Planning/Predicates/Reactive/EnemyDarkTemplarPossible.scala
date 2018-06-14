package Planning.Predicates.Reactive

import Lifecycle.With
import Planning.Predicate
import ProxyBwapi.Races.Protoss

class EnemyDarkTemplarPossible extends Predicate {
  
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
