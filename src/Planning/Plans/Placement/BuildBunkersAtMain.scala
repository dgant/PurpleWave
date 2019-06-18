package Planning.Plans.Placement

import Information.Geography.Types.Base
import Lifecycle.With
import Macro.Architecture.Heuristics.{PlacementProfile, PlacementProfiles}

class BuildBunkersAtMain(
  towersRequired: Int = 1,
  placement: PlacementProfile = PlacementProfiles.hugWorkersWithCannon)
  extends BuildBunkersAtBases(
    towersRequired,
    placement) {
  
  override def eligibleBases: Iterable[Base] = Seq(With.geography.ourMain)
}