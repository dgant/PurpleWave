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
    TargetFilterAnchor,
    TargetFilterCarrierIgnoreInterceptors)

  val filtersForSimulation: Vector[TargetFilter] = filtersRequired.filter(_.simulationSafe)

  val filtersPreferred = Vector(
    TargetFilterCarrierInRange,
    TargetFilterCarrierInLeash,
    TargetFilterCarrierShootsUp,
    TargetFilterCombatants,
    TargetFilterFrontline,
    TargetFilterIgnoreScouts,
  )
}
