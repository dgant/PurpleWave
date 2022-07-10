package Information.Fingerprinting

import Debugging.ToString
import Lifecycle.With
import Utilities.Time.{Forever, Minutes}

abstract class Fingerprint {
  private var _lastUpdateFrame: Int = -Forever()
  protected var matched: Boolean = false
  
  protected def sticky: Boolean = false
  protected def lockAfter: Int = Minutes(10)()
  protected def investigate: Boolean
  protected val children: Seq[Fingerprint] = Seq.empty

  @inline final def lastUpdateFrame: Int = _lastUpdateFrame
  //@inline final def matches: Boolean = matched
  @inline final def apply(): Boolean = matched
  @inline final def recently: Boolean = matched || With.strategy.enemyRecentFingerprints.contains(toString)
  @inline final def update(): Unit = {
    if (_lastUpdateFrame == With.frame) return
    _lastUpdateFrame = With.frame
    children.foreach(_.update())
    if (sticky && matched) return
    if (With.frame < lockAfter) {
      val matchedBefore = matched
      matched = investigate
      if (matched != matchedBefore && With.frame > 0) {
        With.logger.debug(explanation)
      }
    }
  }

  def workerDelta: Int = 0

  protected def reason: String = "(No reason)"
  def explanation: String = f"$this ${if (matched) "matched" else "unmatched"}: $reason"
  override val toString: String = ToString(this).replace("Fingerprint", "Finger")
}
