package Information.StrategyDetection

case class FingerprintAnd(fingerprints: Fingerprint*) extends Fingerprint {
  
  override def matches: Boolean = {
    fingerprints.forall(_.matches)
  }
}
