package Planning.Plans.Placement

import Information.Geography.Types.Base
import Lifecycle.With
import Planning.Plans.Macro.Protoss.BuildTowersAtBases

class BuildCannonsAtNatural(initialCount: Int) extends BuildTowersAtBases(initialCount) {
  
  override def eligibleBases: Vector[Base] = {
    var output = With.geography.ourBasesAndSettlements.filter(With.geography.ourNatural==)
    if (output.isEmpty) {
      output = Vector(With.geography.ourNatural)
    }
    output
  }
}
