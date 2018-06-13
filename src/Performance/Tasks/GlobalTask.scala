package Performance.Tasks

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
    With.coordinator.run()
    With.agents.run()
  }
}
class TaskPlanning extends AbstractTask {
  urgency = With.configuration.urgencyPlanning
  override protected def onRun() {
    With.intelligence.update()
    With.bank.update()
    With.recruiter.update()
    With.prioritizer.update()
    With.scheduler.reset()
    With.squads.reset()
    With.buildOrderHistory.update()
    With.strategy.gameplan.update()
    With.groundskeeper.update()
    With.squads.update()
  }
}
class TaskVisualizations extends AbstractTask {
  override def maxConsecutiveSkips: Int = 0
  override protected def onRun() {
    With.visualization.render()
  }
}
class TaskUnitTracking extends AbstractTask {
  urgency = With.configuration.urgencyUnitTracking
  override def maxConsecutiveSkips: Int = 0
  override protected def onRun() {
    With.units.update()
  }
}
