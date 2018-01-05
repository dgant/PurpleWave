package Micro.Actions.Combat.Attacking

import Micro.Actions.Combat.Attacking.Filters.TargetFilterVisibleInRange

object TargetInRange extends TargetAction(TargetFilterVisibleInRange)