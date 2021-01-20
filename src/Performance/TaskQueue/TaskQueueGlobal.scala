package Performance.TaskQueue
import Lifecycle.With
import Performance.Tasks._

object TaskQueueGlobalWeights {
  var Micro      = 40
  var Battles    = 25
  var Gather     = 5
  var Placement  = 5
  var Planning   = 5
  var Squads     = 5
}

class TaskQueueGlobal extends TaskQueueParallel(
  new TaskQueueSerial("Fingerprinting", With.fingerprints.relevant.map(f => new SimpleTask(f.toString, f.update)): _*),
  With.geography,
  new TaskQueueGrids,
  With.battles,
  With.accounting,
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
    .withSkipsMax(6)
    .withWeight(TaskQueueGlobalWeights.Planning),
  With.gathering,
  new TaskQueueSerial(
    "Squads",
    new SimpleTask("SquadGoals", With.squads.updateGoals),
    new SimpleTask("SquadBatching", With.squads.stepBatching).withAlwaysSafe(true))
    .withWeight(TaskQueueGlobalWeights.Squads),
  With.placement,
  new TaskQueueSerial(
    "Micro",
    new SimpleTask("Matchups", With.matchups.run),
    With.agents)
    .withSkipsMax(1)
    .withWeight(TaskQueueGlobalWeights.Micro),
  With.manners,
  With.camera,
  With.visualization) {
  withName("Global")
}
