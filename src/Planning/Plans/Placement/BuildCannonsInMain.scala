package Planning.Plans.Placement

import Information.Geography.Types.Base
import Lifecycle.With

class BuildCannonsInMain(initialCount: Int) extends BuildTowersAtBases(initialCount) {
  override def eligibleBases: Vector[Base] = Vector(With.geography.ourMain)
}
