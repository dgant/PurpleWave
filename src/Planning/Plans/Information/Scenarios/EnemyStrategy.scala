package Planning.Plans.Information.Scenarios

import Information.Intelligence.Fingerprinting.Fingerprint
import Planning.Plan

class EnemyStrategy(fingerprint: Fingerprint) extends Plan {
  
  override def isComplete: Boolean = fingerprint.matches
}
