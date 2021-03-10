package Performance.TaskQueue
import Lifecycle.With
import Performance.Tasks._

object TaskQueueGlobalWeights {
  var Micro      = 55
  var Battles    = 35
  var Planning   = 6
  var Gather     = 2
  var Placement  = 2
}

class TaskQueueGlobal extends TaskQueueParallel(
  new TaskQueueSerial("Fingerprinting", With.fingerprints.relevant.map(f => new SimpleTask(f.toString, f.update)): _*),
  With.geography,
  new TaskQueueGrids,
  With.battles,
  With.preplacement,
  With.yolo,
  With.scouting,
  new TaskQueueSerial(
    "Planning",
    With.accounting,
    With.unitsShown,
    With.buildOrderHistory,
    new SimpleTask("Gameplan", () => {
      With.bank.update()
      With.recruiter.update()
      With.prioritizer.update()
      With.scheduler.reset() // Synchronous with gameplan; Flickers ShowProduction otherwise
      With.blackboard.reset() // Synchronous with gameplan; Flickers flags otherwise
      With.strategy.gameplan.update()
      if (With.yolo.active()) With.blackboard.wantToAttack.set(true)
    }),
    With.tactics,
    With.squads,
    With.gathering,
    With.groundskeeper)
    .withSkipsMax(6)
    .withWeight(TaskQueueGlobalWeights.Planning),
  With.placement,
  new TaskQueueSerial(
    "Micro",
    With.matchups,
    With.agents)
    .withSkipsMax(1)
    .withWeight(TaskQueueGlobalWeights.Micro),
  With.manners,
  With.camera,
  With.visualization) {
  withName("Global")
}
