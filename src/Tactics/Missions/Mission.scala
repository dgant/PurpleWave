package Tactics.Missions

import Lifecycle.With
import ProxyBwapi.UnitInfo.FriendlyUnitInfo
import Tactics.Squads.Squad

trait Mission extends Squad {
  protected def shouldForm: Boolean
  protected def shouldTerminate: Boolean
  protected def recruit(): Unit
  protected def reset(): Unit = {}

  var launched: Boolean = false
  var launchFrame: Int = 0
  final def duration: Int = if (launched) With.framesSince(launchFrame) else 0

  private def nextUnits: Iterable[FriendlyUnitInfo] = With.recruiter.lockedBy(this)
  final def launch(): Unit = {
    if (launched) {
      if (nextUnits.isEmpty) {
        With.logger.debug(f"Terminating $this (nextUnits is empty)")
        terminate()
      } else if (shouldTerminate) {
        With.logger.debug(f"Terminating $this (Termination condition reached)")
        terminate()
      } else {
        With.recruiter.renew(this)
      }
    } else if (shouldForm && ! shouldTerminate) {
      reset()
      launched = true
      launchFrame = With.frame
      recruit()
      With.logger.debug(f"Launching $this to ${vicinity.base.getOrElse(vicinity.tile)} with ${unitsNext.view.map(_.toString).mkString(", ")}")
    }
    if (launched) {
      addUnits(nextUnits)
      With.recruiter.locksOf(this).foreach(_.interruptable = false)
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
    reset()
  }
}
