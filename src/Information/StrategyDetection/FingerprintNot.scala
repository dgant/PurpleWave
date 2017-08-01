package Information.StrategyDetection

case class FingerprintNot(fingerprint: Fingerprint) extends Fingerprint {
  
  override def matches: Boolean = ! fingerprint.matches
}
