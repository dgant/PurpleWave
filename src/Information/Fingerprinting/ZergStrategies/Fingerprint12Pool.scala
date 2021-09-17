package Information.Fingerprinting.ZergStrategies

import Information.Fingerprinting.Generic.{FingerprintAnd, FingerprintCompleteBy, FingerprintNot}
import Information.Fingerprinting.Strategies.ZergTimings
import Lifecycle.With
import ProxyBwapi.Races.Zerg
import Utilities.Time.Seconds

class Fingerprint12Pool extends FingerprintAnd(
  new FingerprintNot(With.fingerprints.fourPool),
  new FingerprintNot(With.fingerprints.ninePool),
  new FingerprintNot(With.fingerprints.overpool),
  new FingerprintCompleteBy(Zerg.SpawningPool, ZergTimings.TwelvePool_PoolCompleteBy + Seconds(10)) // Going past 10 seconds intersects10h9p
)
