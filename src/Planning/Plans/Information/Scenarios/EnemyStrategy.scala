package Planning.Plans.Information.Scenarios

import Information.Intelligenze.Fingerprinting.Fingerprint
import Planning.Plan

class EnemyStrategy(fingerprint: Fingerprint) extends Plan {
  
  override def isComplete: Boolean = fingerprint.matches
}
