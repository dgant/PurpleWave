package Planning.Plans.Macro.Automatic

import Lifecycle.With
import Planning.Plan

class CapGasAt(
  floor: Int,
  ceiling: Int = Int.MinValue,
  ratio: Double = Double.NaN) extends Plan {
  
  override def onUpdate() {
    val finalCeiling = if (ceiling == Int.MinValue) floor else ceiling
    
    With.blackboard.gasLimitFloor.set(floor)
    With.blackboard.gasLimitCeiling.set(finalCeiling)
    
    if ( ! ratio.isNaN) {
      With.blackboard.gasTargetRatio.set(ratio)
    }
  }
}
