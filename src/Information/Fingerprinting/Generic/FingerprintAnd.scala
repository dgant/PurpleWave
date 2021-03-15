package Information.Fingerprinting.Generic

import Information.Fingerprinting.Fingerprint

class FingerprintAnd(fingerprints: Fingerprint*) extends Fingerprint {
  
  override val children: Seq[Fingerprint] = fingerprints
  
  override def investigate: Boolean = fingerprints.forall(_.matches)
}
