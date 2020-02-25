package Performance.Tasks

import Information.Intelligenze.Fingerprinting.Generic.GameTime
import Lifecycle.{Manners, With}
import Performance.TaskQueue.TaskQueueGrids

class TaskArchitecture extends AbstractTask {
  urgency = With.configuration.urgencyArchitecture
  override protected def onRun() { With.placement.run() }
}
class TaskAccounting extends AbstractTask {
  urgency = With.configuration.urgencyEconomy
  override protected def onRun() { With.economy.update() }
}
class TaskBattles extends AbstractTask {
  urgency = With.configuration.urgencyBattles
  override def maxConsecutiveSkips: Int = 8
  override protected def onRun() { With.battles.run() }
}
class TaskCamera extends AbstractTask {
  override protected def onRun() { With.camera.onFrame() }
}
class TaskFingerprinting extends AbstractTask {
  override protected def onRun(): Unit = {
    With.fingerprints.update()
  }
}
class TaskGeography extends AbstractTask {
  urgency = With.configuration.urgencyGeography
  override protected def onRun() { With.geography.update() }
}
class TaskGrids extends AbstractTask {
  urgency = With.configuration.urgencyGrids
  private val taskQueue = new TaskQueueGrids
  override protected def onRun() { taskQueue.run() }
}
class TaskLatency extends AbstractTask {
  override def maxConsecutiveSkips: Int = 0
  override protected def onRun() { With.latency.onFrame() }
}
class TaskManners extends AbstractTask {
  urgency = With.configuration.urgencyManners
  override protected def onRun() { Manners.run() }
}
class TaskMicro extends AbstractTask {
  urgency = With.configuration.urgencyMicro
  override def maxConsecutiveSkips: Int = 1
  override protected def onRun() {
    With.matchups.run()
    With.commander.run()
    With.coordinator.runPerTask()
    With.agents.run()
  }
}
class TaskPlanning extends AbstractTask {
  urgency = With.configuration.urgencyPlanning
  override def maxConsecutiveSkips: Int =
    if (With.frame < GameTime(5, 0)())
      3
    else if (With.frame < GameTime(10, 0)())
      6
    else
      12
  override protected def onRun() {
    With.intelligence.update()
    With.yolo.update()
    With.bank.update()
    With.recruiter.update()
    With.prioritizer.update()
    With.scheduler.reset()
    With.squads.clearConscripts()
    With.squads.startNewBatch()
    With.buildOrderHistory.update()
    With.blackboard.reset()
    With.strategy.gameplan.update()
    With.groundskeeper.update()
  }
}
class TaskSquads extends AbstractTask {
  urgency = With.configuration.urgencySquads
  override protected def onRun(): Unit = {
    With.squads.updateGoals()
    With.squads.stepBatching()
  }
}
class TaskVisualizations extends AbstractTask {
  override def maxConsecutiveSkips: Int = 0
  override protected def onRun() {
    With.visualization.render()
    With.animations.render()
  }
}
class TaskUnitTracking extends AbstractTask {
  urgency = With.configuration.urgencyUnitTracking
  override def maxConsecutiveSkips: Int = 0
  override protected def onRun() {
    With.units.update()
  }
}
