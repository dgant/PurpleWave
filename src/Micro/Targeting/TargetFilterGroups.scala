package Micro.Targeting

import Micro.Targeting.FiltersRequired._

object TargetFilterGroups {
  val filtersRequired: Vector[TargetFilter] = Vector(
    TargetFilterEnemy,
    TargetFilterPossible,
    TargetFilterMissing,
    TargetFilterLarvaAndEgg,
    TargetFilterFocus,
    TargetFilterStayCloaked,
    TargetFilterScourge,
    TargetFilterReaver,
    TargetFilterRush,
    TargetFilterVulture,
    TargetFilterVsInterceptors,
    TargetFilterVsTank)

  val filtersForSimulation: Vector[TargetFilter] = filtersRequired.filter(_.simulationSafe)
}
