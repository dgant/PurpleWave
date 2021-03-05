package Planning.Plans.Macro.Automatic

import Lifecycle.With
import Planning.Predicate

class GasCapsUntouched extends Predicate {
  override def apply: Boolean = (
    ! With.blackboard.gasWorkerCeiling.isSet
    && ! With.blackboard.gasWorkerFloor.isSet
    && ! With.blackboard.gasLimitFloor.isSet
    && ! With.blackboard.gasLimitCeiling.isSet
    && ! With.blackboard.gasWorkerRatio.isSet
  )
}
