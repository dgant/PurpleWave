package Planning.Plans.Macro.Automatic

import Lifecycle.With
import Planning.Plans.Compound.Do

class CapGasAt(value: Int) extends Do(() => {
  With.blackboard.gasLimitFloor = value
  With.blackboard.gasLimitCeiling = value
})
