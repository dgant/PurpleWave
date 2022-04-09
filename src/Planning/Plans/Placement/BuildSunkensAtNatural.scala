package Planning.Plans.Placement

import Information.Geography.Types.Base
import Lifecycle.With
import ProxyBwapi.Races.Zerg

class BuildSunkensAtNatural(towersRequired: Int) extends BuildZergStaticDefenseAtBases(towersRequired, Zerg.SunkenColony ) {
  override def eligibleBases: Iterable[Base] = Vector(With.geography.ourNatural)
}