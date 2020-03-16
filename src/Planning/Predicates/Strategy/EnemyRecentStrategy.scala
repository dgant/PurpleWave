package Planning.Predicates.Strategy

import Information.Fingerprinting.Fingerprint
import Lifecycle.With
import Planning.Plans.Compound.Or
import Planning.Predicate

class EnemyRecentStrategy(fingerprints: Fingerprint*) extends Or(
  new EnemyStrategy(fingerprints: _*),
  new Predicate {
    override def isComplete: Boolean = {
      fingerprints.map(_.toString).exists(With.strategy.enemyRecentFingerprints.contains)
    }
})
