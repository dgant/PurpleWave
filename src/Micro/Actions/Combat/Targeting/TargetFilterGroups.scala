package Micro.Actions.Combat.Targeting

import Micro.Actions.Combat.Targeting.Filters._

object TargetFilterGroups {
  val filtersRequired = Vector(
    TargetFilterPossible,
    TargetFilterLarvaAndEgg,
    TargetFilterSquad,
    TargetFilterCloaked,
    TargetFilterScourge,
    TargetFilterReaver,
    TargetFilterRush,
    TargetFilterVulture,
    TargetFilterFutility,
    TargetFilterCarrierIgnoreInterceptors)

  val filtersPreferred = Vector(
    TargetFilterCarrierInRange,
    TargetFilterCarrierInLeash,
    TargetFilterCarrierShootsUp,
    TargetFilterCombatants,
    TargetFilterFrontline,
    TargetFilterIgnoreScouts,
  )
}
