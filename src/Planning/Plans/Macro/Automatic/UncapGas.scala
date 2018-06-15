package Planning.Plans.Macro.Automatic

import Lifecycle.With
import Planning.Plans.Compound.Do

class UncapGas extends Do(() => {
  With.blackboard.gasLimitFloor = 0
  With.blackboard.gasLimitCeiling = 100000
})
