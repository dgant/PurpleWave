package Planning.Plans.Placement

import Information.Geography.Types.Base
import Lifecycle.With
import Mathematics.Maff

class BuildBunkersAtEnemy(towersRequired: Int = 1) extends BuildBunkersAtBases(towersRequired) {
  override def eligibleBases: Iterable[Base] = Maff.minBy(With.geography.enemyBases)(_.heart.groundPixels(With.geography.home))
}