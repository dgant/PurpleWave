package Planning.Predicates.Reactive

import Lifecycle.With
import Planning.Predicate
import ProxyBwapi.Races.Protoss

class EnemyDarkTemplarLikely extends Predicate {
  
  override def isComplete: Boolean = (
    With.units.existsEnemy(Protoss.DarkTemplar)
    || (
      (
      With.units.existsEnemy(
        Protoss.HighTemplar,
        Protoss.Archon,
        Protoss.DarkArchon,
        Protoss.TemplarArchives,
        Protoss.ArbiterTribunal,
        Protoss.Arbiter)
      || (
        With.units.existsEnemy(Protoss.CitadelOfAdun)
        && With.units.countEnemy(Protoss.Gateway) < 3
        && ! With.units.enemy.exists(u => u.is(Protoss.CyberneticsCore) && u.upgrading)))
      && ! With.fingerprints.fourGateGoon.matches
  ))
}
