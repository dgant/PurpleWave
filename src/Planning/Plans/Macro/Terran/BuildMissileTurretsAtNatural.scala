package Planning.Plans.Macro.Terran

import Information.Geography.Types.Base
import Lifecycle.With
import Macro.Architecture.Heuristics.{PlacementProfile, PlacementProfiles}

class BuildMissileTurretsAtNatural(
  towersRequired: Int = 1,
  placement: PlacementProfile = PlacementProfiles.hugWorkersWithCannon)
  extends BuildMissileTurretsAtBases(
    towersRequired,
    placement) {

  override def eligibleBases: Iterable[Base] = Seq(With.geography.ourNatural)
}