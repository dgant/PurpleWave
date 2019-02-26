package Planning.Plans.Macro.Automatic

import Lifecycle.With
import Planning.Plan

class CapGasAt(
  a: Int,
  b: Int = Int.MinValue,
  ratio: Double = Double.NaN) extends Plan {

  override def onUpdate() {
    val finalCeiling = if (b == Int.MinValue) a else b

    With.blackboard.gasLimitFloor.set(a)
    With.blackboard.gasLimitCeiling.set(finalCeiling)
    
    if ( ! ratio.isNaN) {
      With.blackboard.gasTargetRatio.set(ratio)
    }
  }
}
