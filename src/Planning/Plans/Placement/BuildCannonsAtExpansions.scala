package Planning.Plans.Placement

import Information.Geography.Types.Base
import Lifecycle.With

class BuildCannonsAtExpansions(initialCount: Int) extends BuildTowersAtBases(initialCount) {
  override def eligibleBases: Iterable[Base] = {
    With.geography.ourBases
      .filterNot(With.geography.ourMain==)
      .filterNot(With.geography.ourNatural==)
  }
}
