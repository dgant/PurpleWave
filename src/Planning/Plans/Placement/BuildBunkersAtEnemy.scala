package Planning.Plans.Placement

import Information.Geography.Types.Base
import Lifecycle.With
import Macro.Architecture.Heuristics.{PlacementProfile, PlacementProfiles}
import Utilities.ByOption

class BuildBunkersAtEnemy(
  towersRequired: Int = 1,
  placement: PlacementProfile = PlacementProfiles.hugWorkersWithCannon)
  extends BuildBunkersAtBases(
    towersRequired,
    placement) {
  
  override def eligibleBases: Iterable[Base] = ByOption.minBy(With.geography.enemyBases)(_.heart.groundPixels(With.geography.home))
}