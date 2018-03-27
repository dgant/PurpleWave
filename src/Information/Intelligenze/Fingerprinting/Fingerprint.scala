package Information.Intelligenze.Fingerprinting

abstract class Fingerprint {
  
  protected val sticky: Boolean = false
  protected def investigate: Boolean
  protected val children: Seq[Fingerprint] = Seq.empty
  
  private var matched: Boolean = false
  final def matches: Boolean = matched
  final def update() {
    children.foreach(_.update())
    if (sticky && matched) return
    matched = investigate
  }
  
  override def toString: String = {
    getClass.getSimpleName.replaceAllLiterally("$", "")
  }
}
