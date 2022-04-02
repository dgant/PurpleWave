package Information.Fingerprinting.Generic

import Information.Fingerprinting.Fingerprint

class FingerprintOr(fingerprints: Fingerprint*) extends Fingerprint {
  
  override val children: Seq[Fingerprint] = fingerprints

  override def reason: String = f"Matched children: [${children.view.filter(_()).map(_.explanation).mkString("; ")}]"
  
  override def investigate: Boolean = fingerprints.exists(_())
}
