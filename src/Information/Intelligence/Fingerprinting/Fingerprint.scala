package Information.Intelligence.Fingerprinting

abstract class Fingerprint {
  
  private var triggered: Boolean = false
  var trigger: Boolean = false
  
  final def matches: Boolean = {
    if (trigger && triggered) return true
    triggered = investigate
    triggered
  }
  
  protected def investigate: Boolean
  
  override def toString: String = {
    getClass.getSimpleName.replaceAllLiterally("$", "")
  }
}
