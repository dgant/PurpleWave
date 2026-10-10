package Tactic.Missions

import Lifecycle.With
import ProxyBwapi.UnitInfo.FriendlyUnitInfo
import Tactic.Squads.Squad

trait Mission extends Squad {
  protected def shouldForm: Boolean
  protected def recruit(): Unit
  protected def reset(): Unit = {}
  protected def onRun(): Unit = {}

  var launched: Boolean = false
  var launchFrame: Int = 0
  final def duration: Int = if (launched) With.framesSince(launchFrame) else 0

  private def nextUnits: Iterable[FriendlyUnitInfo] = With.recruiter.lockedBy(this)
  final def launch(): Unit = {
    if (launched) {
      With.recruiter.renew(this)
    } else if (shouldForm) {
      reset()
      launched = true
      launchFrame = With.frame
      recruit()
      With.logger.debug(f"Launching $this to ${vicinity.base.getOrElse(vicinity.tile)} with ${With.recruiter.lockedBy(this).view.map(_.toString).mkString(", ")}")
    }
    if (launched) {
      With.squads.commission(this)
    }
    With.recruiter.locksOf(this).foreach(_.interruptable = false)
  }

  final def terminate(reason: String = ""): Unit = {
    if (launched) {
      With.logger.debug(f"Terminating $this: $reason")
    }
    launched = false
    With.recruiter.release(this)
  }
}
