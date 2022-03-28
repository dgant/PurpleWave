package Planning.Plans.Macro.Protoss

import Information.Geography.Types.Base
import Lifecycle.With

class BuildCannonsAtNaturalAndExpansions(initialCount: Int) extends BuildTowersAtBases(initialCount) {
  
  override def eligibleBases: Vector[Base] = {
    With.geography.ourBases.filterNot(With.geography.ourMain==)
  }
}
