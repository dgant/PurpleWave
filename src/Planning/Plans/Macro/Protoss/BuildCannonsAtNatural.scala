package Planning.Plans.Macro.Protoss

import Information.Geography.Types.Base
import Lifecycle.With

class BuildCannonsAtNatural(initialCount: Int) extends BuildTowersAtBases(initialCount) {
  
  override def eligibleBases: Vector[Base] = {
    var output = With.geography.ourBasesAndSettlements.filter(_ == With.geography.ourNatural)
    if (output.isEmpty) {
      output = Vector(With.geography.ourNatural)
    }
    output
  }
}
