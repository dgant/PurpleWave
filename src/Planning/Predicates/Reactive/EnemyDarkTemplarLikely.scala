package Planning.Predicates.Reactive

import Lifecycle.With
import Planning.Predicate
import ProxyBwapi.Races.Protoss

class EnemyDarkTemplarLikely extends Predicate {
  
  override def isComplete: Boolean = (
    With.units.existsEnemy(Protoss.DarkTemplar)
    || (
      With.units.enemy.exists(_.isAny(
        Protoss.HighTemplar,
        Protoss.Archon,
        Protoss.DarkArchon,
        Protoss.CitadelOfAdun,
        Protoss.TemplarArchives,
        Protoss.ArbiterTribunal,
        Protoss.Arbiter))
      && ! With.fingerprints.fourGateGoon.matches
  ))
}
