package Performance.TaskQueue
import Performance.Tasks._

class TaskQueueGlobal extends AbstractTaskQueue {
  
  override val tasks: Vector[AbstractTask] = Vector (
    new TaskLatency,
    new TaskUnitTracking,
    new TaskFingerprinting,
    new TaskGeography,
    new TaskGrids,
    new TaskBattles,
    new TaskAccounting,
    new TaskPlanning,
    new TaskSquads,
    new TaskArchitecture,
    new TaskMicro,
    new TaskManners,
    new TaskCamera,
    new TaskVisualizations
  )
}
