package Planning.Predicates.Strategy

import Information.Fingerprinting.Fingerprint
import Planning.Predicates.{MacroFacts, Predicate}

case class EnemyStrategy(fingerprints: Fingerprint*) extends Predicate {
  override def apply: Boolean = MacroFacts.enemyStrategy(fingerprints: _*)
}
