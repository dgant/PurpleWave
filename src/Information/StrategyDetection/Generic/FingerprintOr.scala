package Information.StrategyDetection.Generic

import Information.StrategyDetection.Fingerprint

case class FingerprintOr(fingerprints: Fingerprint*) extends Fingerprint {
  
  override def matches: Boolean = {
    fingerprints.exists(_.matches)
  }
}
