package Planning.Plans.Placement

import Information.Geography.Types.Base
import Lifecycle.With

class BuildBunkersAtNatural(towersRequired: Int = 1) extends BuildBunkersAtBases(towersRequired) {
  override def eligibleBases: Iterable[Base] = Seq(With.geography.ourNatural)
}