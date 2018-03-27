package Information.Intelligenze.Fingerprinting.Generic

import Information.Intelligenze.Fingerprinting.Fingerprint

class FingerprintAnd(fingerprints: Fingerprint*) extends Fingerprint {
  
  override val children: Seq[Fingerprint] = fingerprints
  
  override def investigate: Boolean = {
    fingerprints.foreach(_.matches) // Hack -- we need all fingerprints to update each time.
    fingerprints.forall(_.matches)
  }
}
