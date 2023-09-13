package Planning.Predicates.Strategy

import Information.Fingerprinting.Fingerprint
import Planning.MacroFacts
import Planning.Predicates.Predicate

case class EnemyRecentStrategy(fingerprints: Fingerprint*) extends Predicate {
  override def apply: Boolean = MacroFacts.enemyRecentStrategy(fingerprints: _*)
}

