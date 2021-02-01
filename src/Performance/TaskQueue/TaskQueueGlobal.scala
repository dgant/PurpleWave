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
  new TaskQueueSerial(
    "Planning",
    new SimpleTask("UnitsShown",        With.unitsShown.update),
    new SimpleTask("Scouting",          With.scouting.update),
    new SimpleTask("YOLO",              With.yolo.update),
    new SimpleTask("Preplacement",      With.preplacement.update),
    new SimpleTask("BuildOrderHistory", With.buildOrderHistory.update),
    new SimpleTask("Gameplan", () => {
      With.squads.clearConscripts() // Synchronous with gameplan; clearing squads leaves units squadless in the inteirm
      With.squads.startNewBatch() // Synchronous with gameplan; clearing squads leaves units squadless in the inteirm
      With.bank.update()
      With.recruiter.update()
      With.prioritizer.update()
      With.scheduler.reset() // Synchronous with gameplan; Flickers ShowProduction otherwise
      With.blackboard.reset() // Synchronous with gameplan; Flickers flags otherwise
      With.strategy.gameplan.update()
      With.tactics.update()
    }),
    //new SimpleTask(With.tactics.update),
    new SimpleTask(With.groundskeeper.update))
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
