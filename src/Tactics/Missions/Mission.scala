package Tactics.Missions

import Lifecycle.With
import Tactics.Squads.Squad

trait Mission extends Squad {
  protected def shouldForm: Boolean
  protected def shouldTerminate: Boolean
  protected def recruit(): Unit

  var launched: Boolean = false
  var launchFrame: Int = 0
  final def duration: Int = if (launched) With.framesSince(launchFrame) else 0

  final def consider(): Unit = {
    lazy val nextUnits = With.recruiter.lockedBy(this)
    if (launched) {
      if (shouldTerminate || nextUnits.isEmpty) {
        terminate()
      } else {
        With.recruiter.renew(this)
      }
    } else if (shouldForm && ! shouldTerminate) {
      With.logger.debug(f"Launching $this")
      launched = true
      launchFrame = With.frame
      recruit()
    }
    if (launched) {
      addUnits(nextUnits)
    } else {
      terminate()
    }
  }

  final def terminate(): Unit = {
    if (launched) {
      With.logger.debug(f"Terminating $this")
    }
    launched = false
    With.recruiter.release(this)
  }
}
