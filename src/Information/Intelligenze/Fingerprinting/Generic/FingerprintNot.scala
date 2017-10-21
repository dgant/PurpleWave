package Information.Intelligenze.Fingerprinting.Generic

import Information.Intelligenze.Fingerprinting.Fingerprint

class FingerprintNot(fingerprint: Fingerprint) extends Fingerprint {
  
  override def investigate: Boolean = ! fingerprint.matches
}
