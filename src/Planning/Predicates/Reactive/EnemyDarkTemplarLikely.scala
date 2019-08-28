package Planning.Predicates.Reactive

import Lifecycle.With
import Planning.Predicate
import ProxyBwapi.Races.Protoss

class EnemyDarkTemplarLikely extends Predicate {
  
  override def isComplete: Boolean = (
    With.fingerprints.dtRush.matches
    || With.units.existsEnemy(
        Protoss.HighTemplar,
        Protoss.Archon,
        Protoss.DarkArchon,
        Protoss.TemplarArchives,
        Protoss.ArbiterTribunal,
        Protoss.Arbiter))
}
