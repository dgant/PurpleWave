package Information.Intelligence.Fingerprinting.Generic

import Information.Intelligence.Fingerprinting.Fingerprint

class FingerprintNot(fingerprint: Fingerprint) extends Fingerprint {
  
  override def investigate: Boolean = ! fingerprint.matches
}
