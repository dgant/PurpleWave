package Planning

import Lifecycle.With
import Performance.Cache
import Performance.Tasks.TimedTask
import Planning.UnitMatchers.MatchWorker
import ProxyBwapi.Races.Protoss
import Utilities.Time.Minutes

class Yolo extends TimedTask {

  private def activeByDefault: Boolean = With.blackboard.yoloing() && (
    ! With.units.existsOurs(MatchWorker)
    || With.geography.ourBases.forall(_.mineralsLeft == 0)
    || With.blackboard.allIn())

  private var lastUpdate: Int = 0
  private var maxoutFramesCharged: Int = 0
  private var maxoutYolo: Boolean = false
  private val maxoutYoloFrameThreshold = Minutes(2)()

  private def maxouted = With.self.supplyUsed / 2 >= 192 && With.units.ours.forall(u => ! u.is(Protoss.Carrier) || u.interceptorCount > 7)

  val active = new Cache(() => activeByDefault || maxoutYolo)

  override protected def onRun(budgetMs: Long): Unit = {
    var frames = With.framesSince(lastUpdate)
    lastUpdate = With.frame
    if (maxouted && With.frame > Minutes(20)()) {
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

  def forceBlackboard(): Unit = {
    if (active()) {
      With.blackboard.wantToAttack.set(true)
      With.blackboard.wantToHarass.set(true)
      With.blackboard.safeToMoveOut.set(true)
    }
  }
}
