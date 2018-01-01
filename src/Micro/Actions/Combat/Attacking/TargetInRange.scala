package Micro.Actions.Combat.Attacking

import Micro.Actions.Combat.Attacking.Filters.TargetFilterInRange

object TargetInRange extends TargetAction {
  
  override val additionalFilters = Vector(TargetFilterInRange)
}
