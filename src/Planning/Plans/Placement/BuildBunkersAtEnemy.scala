package Planning.Plans.Placement

import Information.Geography.Types.Base
import Lifecycle.With
import Macro.Architecture.Heuristics.{PlacementProfile, PlacementProfiles}
import Mathematics.Maff


class BuildBunkersAtEnemy(
  towersRequired: Int = 1,
  placement: PlacementProfile = PlacementProfiles.hugWorkersWithCannon)
  extends BuildBunkersAtBases(
    towersRequired,
    placement) {
  
  override def eligibleBases: Iterable[Base] = Maff.minBy(With.geography.enemyBases)(_.heart.groundPixels(With.geography.home))
}