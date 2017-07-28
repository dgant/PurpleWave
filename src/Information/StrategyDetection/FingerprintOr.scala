package Information.StrategyDetection

case class FingerprintOr(fingerprints: Fingerprint*) extends Fingerprint {
  
  override def matches: Boolean = {
    fingerprints.exists(_.matches)
  }
}
