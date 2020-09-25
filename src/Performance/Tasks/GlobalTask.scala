package Performance.Tasks

import Information.Fingerprinting.Generic.GameTime
import Lifecycle.{Manners, With}

class TaskArchitecture extends AbstractTask {
  urgency = With.configuration.urgencyArchitecture
  override protected def onRun() { With.placement.update() }
}
class TaskAccounting extends AbstractTask {
  urgency = With.configuration.urgencyEconomy
  override protected def onRun() { With.economy.update() }
}
class TaskBattles extends AbstractTask {
  urgency = With.configuration.urgencyBattles
  override def maxConsecutiveSkips: Int = 3
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
class TaskGathering extends AbstractTask {
  urgency = With.configuration.urgencyGather
  override protected def onRun(): Unit = {
    With.gathering.run()
  }
}
class TaskGeography extends AbstractTask {
  urgency = With.configuration.urgencyGeography
  override protected def onRun() { With.geography.update() }
}
class TaskGrids extends AbstractTask {
  urgency = With.configuration.urgencyGrids
  override protected def onRun() { With.grids.tasks.run() }
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
    With.unitsShown.update()
    With.scouting.update()
    With.yolo.update()
    With.bank.update()
    With.recruiter.update()
    With.prioritizer.update()
    With.preplacement.update()

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
