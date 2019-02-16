package Planning.Plans.Macro.Automatic

import Lifecycle.With
import Planning.Plan

class FloorGasWorkersAt(floor: Int) extends Plan {
  
  override def onUpdate() {
    With.blackboard.gasWorkerFloor.set(floor)
  }
}
