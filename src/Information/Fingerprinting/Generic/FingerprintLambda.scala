package Information.Fingerprinting.Generic

import Information.Fingerprinting.Fingerprint

class FingerprintLambda(predicate: () => Boolean, isSticky: () => Boolean = () => true) extends Fingerprint {
  override protected def investigate: Boolean = predicate()
  override def sticky: Boolean = isSticky()
}
