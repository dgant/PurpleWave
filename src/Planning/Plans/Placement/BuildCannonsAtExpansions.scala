package Planning.Plans.Placement

import Information.Geography.Types.Base
import Lifecycle.With
import Macro.Architecture.Heuristics.PlacementProfiles
import Planning.Plans.Macro.Protoss.BuildTowersAtBases

class BuildCannonsAtExpansions(initialCount: Int) extends BuildTowersAtBases(
  initialCount,
  PlacementProfiles.hugTownHall,
  PlacementProfiles.hugTownHall) {
  
  override def eligibleBases: Iterable[Base] = {
    With.geography.ourBases
      .filterNot(_ == With.geography.ourMain)
      .filterNot(_ == With.geography.ourNatural)
  }
}
