package Planning.Plans.Macro.Automatic

import Lifecycle.With
import Planning.Plans.Basic.Do

class CapGasAtRatioToMinerals(ratio: Double, margin: Int) extends Do(() => {
  val cap = (With.self.minerals * ratio + margin).toInt
  With.blackboard.gasLimitFloor.set(cap)
  With.blackboard.gasLimitCeiling.set(cap)
})
