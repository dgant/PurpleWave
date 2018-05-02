package Planning.Plans.GamePlans.Protoss.Situational

import Lifecycle.With
import ProxyBwapi.Races.{Protoss, Zerg}

class DefendFFEWithProbesAgainst4Pool extends DefendFFEWithProbes {
  
  override def probeCount: Int = {
    val zerglings           = Math.max(8, With.units.countEnemy(Zerg.Zergling))
    val cannons             = With.units.ours.filter(_.is(Protoss.PhotonCannon))
    val cannonsIncomplete   = cannons.count( ! _.complete)
    val cannonsComplete     = cannons.count(_.complete)
    val workerCount         = With.units.countOurs(_.unitClass.isWorker)
    val workersToMine       = if (cannonsIncomplete == 2) 0 else if (cannonsIncomplete == 1) 3 else 5
    val workersDesired      = if (cannonsComplete >= 5) 0 else Math.min(workerCount - workersToMine, zerglings * 4 - cannonsComplete * 3)
    workersDesired
  }
}
