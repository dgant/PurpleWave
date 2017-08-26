package Information.StrategyDetection

abstract class Fingerprint {
  
  def matches: Boolean
  
  override def toString: String = {
    getClass.getSimpleName.replaceAllLiterally("$", "")
  }
}
