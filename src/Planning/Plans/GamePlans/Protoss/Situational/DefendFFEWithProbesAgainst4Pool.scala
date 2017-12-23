package Planning.Plans.GamePlans.Protoss.Situational

import Lifecycle.With
import ProxyBwapi.Races.Protoss

class DefendFFEWithProbesAgainst4Pool extends DefendFFEWithProbes {
  
  override def probeCount: Int = {
    var cannons         = With.units.ours.filter(_.is(Protoss.PhotonCannon))
    val workerCount     = With.units.ours.count(_.unitClass.isWorker)
    val workersCap      = workerCount - 4
    val workersDesired  = 12 - 3 * cannons.count(_.complete)
    val workersFinal    = Math.max(0, Math.min(workersCap, workersDesired))
    workersFinal
  }
}
