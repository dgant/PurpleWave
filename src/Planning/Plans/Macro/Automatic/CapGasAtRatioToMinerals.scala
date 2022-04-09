package Planning.Plans.Macro.Automatic

import Lifecycle.With
import Planning.Plans.Basic.Write
import Planning.Plans.Compound.Parallel

class CapGasAtRatioToMinerals(ratio: Double, margin: Int = 0) extends Parallel(
  new Write(With.blackboard.gasLimitFloor, () => (With.self.minerals * ratio + margin).toInt),
  new Write(With.blackboard.gasLimitCeiling, () => (With.self.minerals * ratio + margin).toInt))
