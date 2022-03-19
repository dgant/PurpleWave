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
  With.geography,
  With.grids,
  With.preplacement,
  With.scouting,
  new TaskQueueSerial("Fingerprinting", With.fingerprints.relevant.map(f => new SimpleTask(f.toString, f.update)): _*),
  With.yolo,
  With.battles,
  new TaskQueueSerial(
    "Planning",
    With.accounting,
    With.unitsShown,
    With.productionHistory,
    new SimpleTask("Gameplan", () => {
      With.bank.update()
      With.recruiter.update()
      With.prioritizer.update()
      With.scheduler.reset() // Synchronous with gameplan; Flickers ShowProduction otherwise
      With.blackboard.reset() // Synchronous with gameplan; Flickers flags otherwise
      With.blackboard.safeToMoveOut.set(GlobalSafeToMoveOut())
      With.yolo.updateBlackboard()
      With.strategy.update()
      With.strategy.gameplan.update()
      With.macroSim.simulate()
      With.yolo.updateBlackboard() // YOLO trumps gameplan
    }),
    With.tactics,
    // We are running Tactics.Squads inside Tactics for the moment,
    // because when Tactics adds enemies it clears out the Squad's current enemies, potentially leaving it without enemies for a frame.
    // Squads are relatively expensive to run so we should revert this state of affairs.
    // We should also seek to ensure that squads are a run-to-completion batch, eg. we run every squad before proceeding, and not necessarily on the same frame
    // With.squads,
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
