package Planning.Plans.Placement

import Information.Geography.Types.Base
import Lifecycle.With
import ProxyBwapi.Races.Terran

class BuildMissileTurretsAtNatural(initialCount: Int) extends BuildTowersAtBases(initialCount, Terran.MissileTurret) {
  override def eligibleBases: Vector[Base] = Vector(With.geography.ourNatural)
}