package Planning.Plans.Macro.Protoss

import Information.Geography.Types.Base
import Lifecycle.With

class BuildCannonsAtNaturalAndExpansions(initialCount: Int) extends BuildTowersAtBases(initialCount) {
  
  override def eligibleBases: Vector[Base] = {
    var output = With.geography.ourBasesAndSettlements.filter(_ != With.geography.ourMain)
    if (output.isEmpty) {
      output = Vector(With.geography.ourNatural)
    }
    output
  }
}
