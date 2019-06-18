package Planning.Plans.Placement

import Information.Geography.Types.Base
import Lifecycle.With
import Macro.Architecture.Heuristics.PlacementProfiles

class BuildCannonsAtExpansions(initialCount: Int) extends BuildCannonsAtBases(
  initialCount,
  PlacementProfiles.hugTownHall,
  PlacementProfiles.hugTownHall) {
  
  override def eligibleBases: Iterable[Base] = {
    With.geography.ourBases
      .filterNot(_ == With.geography.ourMain)
      .filterNot(_ == With.geography.ourNatural)
  }
}
