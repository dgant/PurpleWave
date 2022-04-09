package Planning.Plans.Placement

import Information.Geography.Types.Base
import Lifecycle.With
import Planning.Plans.Macro.Protoss.BuildTowersAtBases
import ProxyBwapi.Races.Zerg

class BuildSunkensAtExpansions(initialCount: Int) extends BuildTowersAtBases(initialCount, Zerg.SunkenColony) {
  override def eligibleBases: Iterable[Base] = {
    With.geography.ourBasesAndSettlements
      .filterNot(With.geography.ourMain==)
      .filterNot(With.geography.ourNatural==)
  }
}