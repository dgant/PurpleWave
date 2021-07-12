package Performance.TaskQueue
import Information.Battles.GlobalSafeToMoveOut
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
  With.grids,
  With.preplacement,
  With.yolo,
  With.battles,
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
      With.blackboard.safeToMoveOut.set(GlobalSafeToMoveOut())
      With.yolo.forceBlackboard()
      With.strategy.gameplan.update()
      With.yolo.forceBlackboard() // YOLO trumps gameplan
    }),
    With.tactics,
    // With.squads,
    // Running Tactics.Squads inside Tactics for the moment,
    // because when Tactics adds enemies it clears out the Squad's current enemies, potentially leaving it without enemies for a frame
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
  With.manners) {
  withName("Global")
}
