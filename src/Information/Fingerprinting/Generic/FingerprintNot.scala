package Information.Fingerprinting.Generic

import Information.Fingerprinting.Fingerprint

class FingerprintNot(fingerprints: Fingerprint*) extends Fingerprint {
  
  override val children: Seq[Fingerprint] = fingerprints

  override def reason: String = f"[${children.view.filter(_.matches).map(_.explanation).mkString("; ")}]"
  
  override def investigate: Boolean = ! fingerprints.exists(_.matches)
}
