package Information.Fingerprinting.Generic

import Information.Fingerprinting.Fingerprint
import Lifecycle.With
import ProxyBwapi.Races.Terran
import Utilities.Time.GameTime
import Utilities.UnitFilters.{IsWarrior, IsWorker}

class FingerprintRampBlock extends Fingerprint {
  var consecutiveFrames = 0
  override protected def investigate: Boolean = {
    if (With.frame > GameTime(4, 30)()) return false
    if (With.units.existsEnemy(IsWarrior)) return false

    val blocker = With.units.enemy.find(u =>
      IsWorker(u)
      && u.zone.edges.exists(e => e.contains(u.pixel) && e.radiusPixels <= Terran.SCV.radialHypotenuse * 6)
      && u.velocity.lengthSquared < 0.001)

    if (blocker.isDefined) {
      consecutiveFrames += 1
    } else {
      consecutiveFrames = 0
    }

    consecutiveFrames > 24
  }

  override protected val sticky: Boolean = true
}
