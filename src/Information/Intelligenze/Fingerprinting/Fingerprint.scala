package Information.Intelligenze.Fingerprinting

import Information.Intelligenze.Fingerprinting.Generic.GameTime
import Lifecycle.With

abstract class Fingerprint {
  
  protected def sticky: Boolean = false
  protected def lockAfter: Int = GameTime(120, 0)()
  protected def investigate: Boolean
  protected val children: Seq[Fingerprint] = Seq.empty

  protected var firstMatchFrame: Int = -1
  protected var matched: Boolean = false
  final def matches: Boolean = matched
  final def update() {
    children.foreach(_.update())
    if (sticky && matched) {
      return
    }
    if (With.frame < lockAfter) {
      matched = investigate
    }
    if (matched && firstMatchFrame < 0) {
      firstMatchFrame = With.frame
    }
  }
  
  override def toString: String = {
    getClass.getSimpleName.replaceAllLiterally("$", "")
  }
}
