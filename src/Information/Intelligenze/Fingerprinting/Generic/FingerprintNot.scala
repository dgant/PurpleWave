package Information.Intelligenze.Fingerprinting.Generic

import Information.Intelligenze.Fingerprinting.Fingerprint

class FingerprintNot(fingerprint: Fingerprint) extends Fingerprint {
  
  override val children: Seq[Fingerprint] = Seq(fingerprint)
  
  override def investigate: Boolean = ! fingerprint.matches
}
