package Planning.Predicates.Strategy

import Information.Fingerprinting.Fingerprint
import Planning.MacroFacts
import Planning.Predicates.Predicate

case class EnemyStrategy(fingerprints: Fingerprint*) extends Predicate {
  override def apply: Boolean = MacroFacts.enemyStrategy(fingerprints: _*)
}
