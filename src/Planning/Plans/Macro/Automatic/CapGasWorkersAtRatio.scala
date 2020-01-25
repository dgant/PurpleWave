package Planning.Plans.Macro.Automatic

import Lifecycle.With
import Planning.Plan

class CapGasWorkersAtRatio(ratio: Double) extends Plan {
  
  override def onUpdate() {
    With.blackboard.gasWorkerRatio.set(ratio)
  }
}
