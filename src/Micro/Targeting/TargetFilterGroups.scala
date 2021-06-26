package Micro.Targeting

import Micro.Targeting.FiltersOptional.{TargetFilterArchonOptional, TargetFilterCarrierInLeash, TargetFilterCarrierInRange, TargetFilterCombatants}
import Micro.Targeting.FiltersRequired._

object TargetFilterGroups {
  val filtersRequired = Vector(
    TargetFilterPossible,
    TargetFilterLarvaAndEgg,
    TargetFilterFocus,
    TargetFilterStayCloaked,
    TargetFilterScourge,
    TargetFilterReaver,
    TargetFilterRush,
    TargetFilterVulture,
    TargetFilterFutility,
    TargetFilterVsInterceptors,
    TargetFilterVsTank)

  val filtersForSimulation: Vector[TargetFilter] = filtersRequired.filter(_.simulationSafe)

  val filtersPreferred = Vector(
    TargetFilterCarrierInRange,
    TargetFilterCarrierInLeash,
    TargetFilterArchonOptional,
    TargetFilterCombatants,
  )
}
