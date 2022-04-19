package Planning.Plans.Placement

import Information.Geography.Types.Base
import Lifecycle.With
import ProxyBwapi.Races.Zerg

class BuildSunkensInMain(initialCount: Int) extends BuildZergStaticDefenseAtBases(initialCount, Zerg.SunkenColony) {
  override def eligibleBases: Iterable[Base] = Vector(With.geography.ourMain)
}