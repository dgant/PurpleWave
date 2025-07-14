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
  With.geography,
  With.grids,
  new TaskQueueSerial(
    "Intel",
    With.scouting,
    With.sense),
  new TaskQueueSerial("Fingerprinting", With.fingerprints.relevant.map(f => new SimpleTask(f.toString, f.update)): _*),
  With.yolo,
  With.battles,
  new TaskQueueSerial(
    "Planning",
    With.accounting,
    With.unitsShown,
    With.productionHistory,
    new SimpleTask("Gameplan", () => {
      With.architecture.update()
      With.bank.update()
      With.recruiter.update()
      With.groundskeeper.update()
      With.priorities.update()
      With.scheduler.reset()
      With.blackboard.reset()
      With.yolo.updateBlackboard() // YOLO affects gameplan
      With.placement.initialize() // It's not relevant at time of writing, but strategy selection may depend on what placements are available
      With.strategy.update()
      With.strategy.gameplan.update()
      With.macroSim.simulate()
      With.yolo.updateBlackboard() // YOLO trumps gameplan
    }),
    With.tactics,
    With.gathering)
    .withSkipsMax(6)
    .withWeight(TaskQueueGlobalWeights.Planning),
  new TaskQueueSerial(
    "Micro",
    With.matchups,
    With.agents)
    .withSkipsMax(1)
    .withWeight(TaskQueueGlobalWeights.Micro),
  With.manners) {



  withName("Global")
}
