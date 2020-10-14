package Micro.Actions.Combat.Targeting

import Micro.Actions.Combat.Targeting.Filters._

object TargetFilterGroups {
  val filtersRequired = Vector(
    TargetFilterPossible,
    TargetFilterLarvaAndEgg,
    TargetFilterLeash,
    TargetFilterFocus,
    TargetFilterStayCloaked,
    TargetFilterFutility,
    TargetFilterScourge,
    TargetFilterReaver,
    TargetFilterCrowded,
    TargetFilterTankFodder,
    TargetFilterRush,
    TargetFilterCarrierIgnoreInterceptors)

  val filtersPreferred = Vector(
    TargetFilterCarrierInRange,
    TargetFilterCarrierInLeash,
    TargetFilterCarrierShootsUp,
    TargetFilterCombatants,
    TargetFilterIgnoreScouts,
  )
}
