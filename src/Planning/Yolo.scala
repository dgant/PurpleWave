package Planning

import Lifecycle.With
import Performance.Tasks.TimedTask
import Planning.Plans.Gameplans.All.MacroActions
import Utilities.UnitFilters.IsWorker
import ProxyBwapi.Races.Protoss
import Utilities.Time.Minutes

class Yolo extends TimedTask with MacroActions {
  var lastUpdate: Int = 0
  var maxoutFramesCharged: Int = 0
  var desperationTriggered: Boolean = false
  var maxoutTriggered: Boolean = false

  def maxed: Boolean = With.units.ours.filter(_.complete).filterNot(u => Protoss.Carrier(u) && u.interceptorCount < 7).map(_.unitClass.supplyRequired).sum / 2 >= 192
  def active: Boolean = desperationTriggered || maxoutTriggered || With.blackboard.yoloing()

  override protected def onRun(budgetMs: Long): Unit = {
    // Trigger desparation
    val weCanNeverMineAgain = ! With.units.existsOurs(IsWorker) || With.geography.ourBases.forall(_.mineralsLeft == 0)
    val enemyIsMining = With.units.existsEnemy(IsWorker) && With.geography.enemyBases.exists(_.mineralsLeft >= 400)
    desperationTriggered = weCanNeverMineAgain && enemyIsMining

    // Trigger mineout
    val frameDelta: Int = With.framesSince(lastUpdate)
    lastUpdate = With.frame
    if (maxed && With.frame > Minutes(15)()) {
      maxoutFramesCharged += frameDelta
    } else {
      maxoutFramesCharged -= 2 * frameDelta
    }

    val maxoutFrameTarget = Minutes(1)() + Minutes(1)() * With.enemies.size * (if (With.self.isTerran) 5 else if (With.enemies.exists(_.isTerran)) 1 else 3)
    if (maxoutFramesCharged <= 0) {
      maxoutFramesCharged = 0
      maxoutTriggered = false
    } else if (maxoutFramesCharged > maxoutFrameTarget){
      maxoutTriggered = true
    }
  }

  def updateBlackboard(): Unit = {
    if (active) {
      With.blackboard.wantToAttack.set(true)
      With.blackboard.wantToHarass.set(true)
    }
    if (maxoutTriggered) {
      status("YOLO: Maxout")
    }
    if (desperationTriggered) {
      status("YOLO: Desperation")
    }
  }
}
