package Information.Intelligenze.Fingerprinting

abstract class Fingerprint {
  
  private var triggered: Boolean = false
  var sticky: Boolean = false
  
  final def matches: Boolean = {
    if (sticky && triggered) {
      return true
    }
    if (investigate) {
      triggered = true
    }
    triggered
  }
  
  protected def investigate: Boolean
  
  override def toString: String = {
    getClass.getSimpleName.replaceAllLiterally("$", "")
  }
}
