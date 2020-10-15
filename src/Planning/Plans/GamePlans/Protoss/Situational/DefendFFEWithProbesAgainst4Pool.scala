package Planning.Plans.GamePlans.Protoss.Situational

import Lifecycle.With
import Planning.UnitMatchers.{UnitMatchAnd, UnitMatchComplete, UnitMatchWorkers}
import ProxyBwapi.Races.{Protoss, Zerg}

class DefendFFEWithProbesAgainst4Pool extends DefendFFEWithProbes {
  
  override def probeCount: Int = {
    val zerglings           = Seq(4, With.units.countEnemy(Zerg.Zergling), 8 - With.units.countEver(Zerg.Zergling)).max
    val cannonsComplete     = With.units.countOurs(UnitMatchAnd(Protoss.PhotonCannon, UnitMatchComplete))
    val cannonsIncomplete   = With.units.countOurs(Protoss.PhotonCannon) - cannonsComplete
    val workerCount         = With.units.countOurs(UnitMatchWorkers)
    val workersToMine       = if (cannonsComplete < 2) 4 else 4 + 2 * cannonsComplete
    val workersDesired      = if (cannonsComplete >= 5) 0 else Math.min(workerCount - workersToMine - With.units.ours.count(_.agent.isScout), zerglings * 4 - cannonsComplete * 3)
    workersDesired
  }
}
