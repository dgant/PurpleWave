package Planning.Plans.Macro.Automatic

import Lifecycle.With
import Planning.Plan

class CapGasWorkersAt(ceiling: Int) extends Plan {
  
  override def onUpdate() {
    With.blackboard.gasWorkerCeiling.set(ceiling)
  }
}
