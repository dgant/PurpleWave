package Planning.Plans.Placement

import Information.Geography.Types.Base
import Lifecycle.With
import Planning.Plans.Macro.Protoss.BuildTowersAtBases
import ProxyBwapi.Races.Terran

class BuildMissileTurretsAtNatural(initialCount: Int) extends BuildTowersAtBases(initialCount, Terran.MissileTurret) {
  override def eligibleBases: Iterable[Base] = Seq(With.geography.ourNatural)
}