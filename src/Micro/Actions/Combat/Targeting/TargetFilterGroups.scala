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
    TargetFilterVsInterceptors,
    TargetFilterVsTank)

  val filtersForSimulation: Vector[TargetFilter] = filtersRequired.filter(_.simulationSafe)

  val filtersPreferred = Vector(
    TargetFilterCarrierInRange,
    TargetFilterCarrierInLeash,
    TargetFilterArchonOptional,
    TargetFilterAntiAir,
    TargetFilterCombatants,
    TargetFilterFrontline,
  )
}
