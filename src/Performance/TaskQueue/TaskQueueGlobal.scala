package Performance.TaskQueue
import Lifecycle.{Manners, With}
import Performance.Tasks._

class TaskQueueGlobal extends TaskQueueParallel(
  new SimpleTask(
    "Fingerprinting",
    With.fingerprints.update),
  new SimpleTask(
    "Geography",
    With.geography.update)
    .withUrgency(With.configuration.urgencyGeography),
  new TaskQueueGrids()
    .withUrgency(With.configuration.urgencyGrids),
  new SimpleTask(
    "Battles",
    With.battles.run)
    .withSkipsMax(3)
    .withUrgency(With.configuration.urgencyBattles),
  new SimpleTask(
    "Accounting",
    With.economy.update)
    .withUrgency(With.configuration.urgencyEconomy),
  new SimpleTask(
    "Planning",
    () => {
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
    })
    .withSkipsMax(4)
    .withUrgency(With.configuration.urgencyPlanning),
  new SimpleTask(
    "Gathering",
    With.gathering.run)
    .withUrgency(With.configuration.urgencyGather),
  new SimpleTask(
    "Squads",
    () => {
      With.squads.updateGoals()
      With.squads.stepBatching()
    })
    .withUrgency(With.configuration.urgencySquads),
  new SimpleTask(
    "Placement",
    With.placement.update)
    .withUrgency(With.configuration.urgencyPlacement),
  new TaskQueueSerial(
    new SimpleTask("Matchups", With.matchups.run),
    new SimpleTask("Commander", With.commander.run),
    new SimpleTask("Agents", With.agents.run))
    .withSkipsMax(1)
    .withUrgency(With.configuration.urgencyMicro),
  new SimpleTask(
    "Manners",
    Manners.run)
    .withUrgency(With.configuration.urgencyManners),
  new SimpleTask(
    "Camera",
    With.camera.onFrame)
    .withSkipsMax(0)
    .withCosmetic(true),
  new SimpleTask(
    "Visuals",
    () => {
      With.visualization.render()
      With.animations.render()
    })
    .withSkipsMax(0)
    .withCosmetic(true))
