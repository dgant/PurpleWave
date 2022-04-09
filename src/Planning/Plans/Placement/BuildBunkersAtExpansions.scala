package Planning.Plans.Placement

import Information.Geography.Types.Base
import Lifecycle.With

class BuildBunkersAtExpansions(towersRequired: Int = 1) extends BuildBunkersAtBases(towersRequired) {
  override def eligibleBases: Iterable[Base] = {
    With.geography.ourBasesAndSettlements
      .filterNot(With.geography.ourMain==)
      .filterNot(With.geography.ourNatural==)
  }
}