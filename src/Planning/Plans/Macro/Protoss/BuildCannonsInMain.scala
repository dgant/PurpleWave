package Planning.Plans.Macro.Protoss

import Information.Geography.Types.Base
import Lifecycle.With
import Macro.Architecture.Heuristics.PlacementProfiles

class BuildCannonsInMain(initialCount: Int) extends BuildCannonsAtBases(
  initialCount,
  PlacementProfiles.hugTownHall,
  PlacementProfiles.hugTownHall) {
  
  override def eligibleBases: Vector[Base] = {
    var output = With.geography.ourBasesAndSettlements.filter(_ == With.geography.ourMain)
    if (output.isEmpty) {
      output = Vector(With.geography.ourMain)
    }
    output
  }
}
