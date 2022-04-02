package Information.Fingerprinting.Generic

import Information.Fingerprinting.Fingerprint

class FingerprintAnd(fingerprints: Fingerprint*) extends Fingerprint {
  
  override val children: Seq[Fingerprint] = fingerprints

  override def reason: String = f"Unmatched children: [${children.view.filterNot(_()).map(_.explanation).mkString("; ")}]"
  
  override def investigate: Boolean = fingerprints.forall(_())
}
