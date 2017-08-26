package Information.StrategyDetection.Generic

import Information.StrategyDetection.Fingerprint

class FingerprintNot(fingerprint: Fingerprint) extends Fingerprint {
  
  override def matches: Boolean = ! fingerprint.matches
}
