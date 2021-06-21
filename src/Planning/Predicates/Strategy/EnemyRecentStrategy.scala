package Planning.Predicates.Strategy

import Information.Fingerprinting.Fingerprint
import Planning.Predicate
import Planning.Predicates.MacroFacts

case class EnemyRecentStrategy(fingerprints: Fingerprint*) extends Predicate {
  override def apply: Boolean = MacroFacts.enemyRecentStrategy(fingerprints: _*)
}

