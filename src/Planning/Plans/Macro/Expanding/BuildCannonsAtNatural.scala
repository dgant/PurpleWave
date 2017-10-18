package Planning.Plans.Macro.Expanding

import Information.Geography.Types.Base
import Lifecycle.With

class BuildCannonsAtNatural(initialCount: Int) extends BuildCannonsAtBases(initialCount) {
  
  override def eligibleBases: Iterable[Base] = {
    var output = With.geography.ourBasesAndSettlements.filter(_ == With.geography.ourNatural)
    if (output.isEmpty) {
      output = Iterable(With.geography.ourNatural)
    }
    output
  }
}
