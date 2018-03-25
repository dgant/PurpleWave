package Planning.Plans.Macro.Protoss

import Information.Geography.Types.Base
import Lifecycle.With

class BuildCannonsAtExpansions(initialCount: Int) extends BuildCannonsAtBases(initialCount) {
  
  override def eligibleBases: Iterable[Base] = {
    With.geography.ourBases
      .filterNot(_ == With.geography.ourMain)
      .filterNot(_ == With.geography.ourNatural)
  }
}
