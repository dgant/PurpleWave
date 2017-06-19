package Performance.TaskQueue
import Performance.Tasks.Global._
import Performance.Tasks._

class TaskQueueGlobal extends AbstractTaskQueue {
  
  override val tasks: Vector[AbstractTask] = Vector (
    new TaskLatency,
    new TaskUnitTracking,
    new TaskGeography,
    new TaskGrids,
    new TaskBattleClassify,
    new TaskBattleEstimator,
    new TaskEconomy,
    new TaskArchitecture,
    new TaskPlanning,
    new TaskMicro,
    new TaskManners,
    new TaskCamera,
    new TaskVisualizations
  )
}
