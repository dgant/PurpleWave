package Information.Fingerprinting.ProtossStrategies

import Information.Fingerprinting.Generic._
import Lifecycle.With
import ProxyBwapi.Races.Protoss
import Utilities.Time.GameTime

class FingerprintNexusFirst extends FingerprintCompleteBy(
  u => Protoss.Nexus(u) && (
    ! u.base.exists(_.isMain)
    || ! u.complete
    || With.scouting.enemyMain.exists( ! u.base.contains(_))),
  GameTime(3, 30), 2) {
  
  override def sticky: Boolean = With.frame > GameTime(3, 20)()
}
