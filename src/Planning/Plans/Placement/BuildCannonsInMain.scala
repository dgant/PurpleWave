package Planning.Plans.Placement

import Information.Geography.Types.Base
import Lifecycle.With
import Macro.Architecture.Heuristics.PlacementProfiles
import Planning.Plans.Macro.Protoss.BuildTowersAtBases

class BuildCannonsInMain(initialCount: Int) extends BuildTowersAtBases(
  initialCount,
  PlacementProfiles.hugTownHall,
  PlacementProfiles.hugTownHall) {
  
  override def eligibleBases: Vector[Base] = {
    var output = With.geography.ourBasesAndSettlements.filter(With.geography.ourMain==)
    if (output.isEmpty) {
      output = Vector(With.geography.ourMain)
    }
    output
  }
}
