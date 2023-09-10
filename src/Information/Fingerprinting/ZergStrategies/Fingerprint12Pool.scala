package Information.Fingerprinting.ZergStrategies

import Information.Fingerprinting.Generic.{FingerprintAnd, FingerprintCompleteBy, FingerprintNot}
import Information.Fingerprinting.ZergStrategies.ZergTimings.Latest_TwelvePool_PoolCompleteBy
import Lifecycle.With
import ProxyBwapi.Races.Zerg

class Fingerprint12Pool extends FingerprintAnd(
  new FingerprintNot(With.fingerprints.fourPool),
  new FingerprintNot(With.fingerprints.ninePool),
  new FingerprintNot(With.fingerprints.overpool),
  new FingerprintCompleteBy(Zerg.SpawningPool, Latest_TwelvePool_PoolCompleteBy) // Going past 10 seconds intersects10h9p
)
