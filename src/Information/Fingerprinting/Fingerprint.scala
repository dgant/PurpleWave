package Information.Fingerprinting

import Lifecycle.With
import Utilities.{Forever, Minutes}

abstract class Fingerprint {
  private var _lastUpdateFrame: Int = -Forever()
  protected var matched: Boolean = false
  
  protected def sticky: Boolean = false
  protected def lockAfter: Int = Minutes(10)()
  protected def investigate: Boolean
  protected val children: Seq[Fingerprint] = Seq.empty

  final def lastUpdateFrame: Int = _lastUpdateFrame
  final def matches: Boolean = matched
  final def update() {
    _lastUpdateFrame = With.frame
    children.foreach(_.update())
    if (sticky && matched) {
      return
    }
    if (With.frame < lockAfter) {
      matched = investigate
    }
  }
  
  override val toString: String = {
    getClass.getSimpleName.replaceAllLiterally("$", "")
  }
}
