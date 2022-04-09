package Planning.Plans.Placement

import Information.Geography.Types.Base
import Lifecycle.With

class BuildBunkersAtEnemyNatural(towersRequired: Int = 1) extends BuildBunkersAtBases(towersRequired) {
  override def eligibleBases: Iterable[Base] = With.geography.enemyBases.flatMap(_.natural)
}