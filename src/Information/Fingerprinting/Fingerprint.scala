package Information.Fingerprinting

import Debugging.ToString
import Lifecycle.With
import Utilities.{Forever, Minutes}

abstract class Fingerprint {
  private var _lastUpdateFrame: Int = -Forever()
  protected var matched: Boolean = false
  
  protected def sticky: Boolean = false
  protected def lockAfter: Int = Minutes(10)()
  protected def investigate: Boolean
  protected val children: Seq[Fingerprint] = Seq.empty

  @inline final def lastUpdateFrame: Int = _lastUpdateFrame
  @inline final def matches: Boolean = matched
  @inline final def apply(): Boolean = matches
  @inline final def update() {
    if (_lastUpdateFrame == With.frame) return
    _lastUpdateFrame = With.frame
    children.foreach(_.update())
    if (sticky && matched) return
    if (With.frame < lockAfter) {
      matched = investigate
    }
  }
  
  override val toString: String = ToString(this).replace("Fingerprint", "Finger")
}
