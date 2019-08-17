package Planning.Plans.Macro.Automatic

import Lifecycle.With
import Planning.Predicate

class GasCapsUnset extends Predicate {
  override def isComplete: Boolean = (
    ! With.blackboard.gasWorkerCeiling.isSet
    && ! With.blackboard.gasWorkerFloor.isSet
    && ! With.blackboard.gasLimitFloor.isSet
    && ! With.blackboard.gasLimitCeiling.isSet
    && ! With.blackboard.gasTargetRatio.isSet
  )
}
