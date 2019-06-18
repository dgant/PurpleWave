package Planning.Plans.Placement

import Information.Geography.Types.Base
import Lifecycle.With

class BuildCannonsAtNatural(initialCount: Int) extends BuildCannonsAtBases(initialCount) {
  
  override def eligibleBases: Vector[Base] = {
    var output = With.geography.ourBasesAndSettlements.filter(_ == With.geography.ourNatural)
    if (output.isEmpty) {
      output = Vector(With.geography.ourNatural)
    }
    output
  }
}
