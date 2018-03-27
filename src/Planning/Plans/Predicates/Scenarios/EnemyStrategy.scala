package Planning.Plans.Predicates.Scenarios

import Information.Intelligenze.Fingerprinting.Fingerprint
import Planning.Plan

class EnemyStrategy(fingerprints: Fingerprint*) extends Plan {
  
  override def isComplete: Boolean = fingerprints.exists(_.matches)
}
