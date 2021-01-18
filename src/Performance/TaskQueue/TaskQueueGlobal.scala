package Performance.TaskQueue
import Lifecycle.{Manners, With}
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
  new SimpleTask("Geography", With.geography.update),
  new TaskQueueGrids(),
  new SimpleTask("Battles", With.battles.run).withAlwaysSafe(true).withSkipsMax(3).withWeight(TaskQueueGlobalWeights.Battles),
  new SimpleTask("Accounting", With.accounting.update),
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
  new SimpleTask("Gathering", With.gathering.run).withWeight(TaskQueueGlobalWeights.Gather),
  new TaskQueueSerial(
    "Squads",
    new SimpleTask("SquadGoals", With.squads.updateGoals),
    new SimpleTask("SquadBatching", With.squads.stepBatching).withAlwaysSafe(true))
    .withWeight(TaskQueueGlobalWeights.Squads),
  new SimpleTask("Placement", With.placement.update).withWeight(TaskQueueGlobalWeights.Placement),
  new TaskQueueSerial(
    "Micro",
    new SimpleTask("Matchups", With.matchups.run),
    new SimpleTask("Commander", With.commander.run).withAlwaysSafe(true),
    new SimpleTask("Agents", With.agents.run).withAlwaysSafe(true))
    .withSkipsMax(1)
    .withWeight(TaskQueueGlobalWeights.Micro),
  new SimpleTask("Manners", Manners.run),
  new SimpleTask("Camera", With.camera.onFrame).withSkipsMax(0).withCosmetic(true),
  new SimpleTask("Visuals", With.visualization.render).withSkipsMax(0).withCosmetic(true))
