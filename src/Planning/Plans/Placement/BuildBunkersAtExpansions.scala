package Planning.Plans.Placement

import Information.Geography.Types.Base
import Lifecycle.With
import Macro.Architecture.Heuristics.{PlacementProfile, PlacementProfiles}

class BuildBunkersAtExpansions(
  towersRequired: Int = 1,
  placement: PlacementProfile = PlacementProfiles.hugWorkersWithCannon)
  extends BuildBunkersAtBases(
    towersRequired,
    placement) {
  
  override def eligibleBases: Iterable[Base] = {
    With.geography.ourBasesAndSettlements
      .filterNot(With.geography.ourMain==)
      .filterNot(With.geography.ourNatural==)
  }
}