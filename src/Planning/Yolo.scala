package Planning

import Information.Intelligenze.Fingerprinting.Generic.GameTime
import Lifecycle.With
import Performance.Cache
import Planning.UnitMatchers.UnitMatchWorkers

class Yolo {

  private def activeByDefault: Boolean = With.blackboard.yoloEnabled() && (
    ! With.units.existsOurs(UnitMatchWorkers)
    || With.geography.ourBases.forall(_.mineralsLeft == 0)
    || With.blackboard.allIn())

  private var lastUpdate: Int = 0
  private var maxoutFramesCharged: Int = 0
  private var maxoutYolo: Boolean = false
  private val maxoutYoloFrameThreshold = GameTime(0, 20)()

  private def maxouted = With.self.supplyUsed / 2 >= 192 && With.units.ours.forall(u => ! u.unitClass.isCarrier || u.interceptorCount > 7)

  def update(): Unit = {
    var frames = With.framesSince(lastUpdate)
    lastUpdate = With.frame
    if (maxouted) {
      maxoutFramesCharged += frames
    } else {
      maxoutFramesCharged -= 2 * frames
    }
    if (maxoutFramesCharged <= 0) {
      maxoutFramesCharged = 0
      maxoutYolo = false
    } else if (maxoutFramesCharged > maxoutYoloFrameThreshold){
      maxoutYolo = true
    }
  }

  val active = new Cache(() => activeByDefault || maxoutYolo)
}
