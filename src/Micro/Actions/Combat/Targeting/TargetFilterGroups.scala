package Micro.Actions.Combat.Targeting

import Micro.Actions.Combat.Targeting.Filters._

object TargetFilterGroups {
  val filtersRequired = Vector(
    TargetFilterPossible,
    TargetFilterLarvaAndEgg,
    TargetFilterLeash,
    TargetFilterFocus,
    TargetFilterStayCloaked,
    TargetFilterScourge,
    TargetFilterReaver,
    TargetFilterTankFodder,
    TargetFilterRush,
    TargetFilterFutility,
    TargetFilterCarrierIgnoreInterceptors)

  val filtersPreferred = Vector(
    TargetFilterCarrierInRange,
    TargetFilterCarrierInLeash,
    TargetFilterCarrierShootsUp,
    TargetFilterCombatants,
    TargetFilterIgnoreScouts,
  )
}
