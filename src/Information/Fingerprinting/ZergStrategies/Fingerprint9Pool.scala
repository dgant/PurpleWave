package Information.Fingerprinting.ZergStrategies

import Information.Fingerprinting.Generic._
import Information.Fingerprinting.ZergStrategies.ZergTimings.{Latest_NinePool_PoolCompleteBy, Latest_NinePool_ZerglingArrivesBy, Latest_NinePool_ZerglingCompleteBy}
import Lifecycle.With
import ProxyBwapi.Races.Zerg

class Fingerprint9Pool extends FingerprintOr(
  With.fingerprints.ninePoolGas,
  new FingerprintAnd(
    new FingerprintNot(With.fingerprints.fourPool),
    new FingerprintOr(
      new FingerprintCompleteBy(Zerg.SpawningPool,  Latest_NinePool_PoolCompleteBy),
      new FingerprintCompleteBy(Zerg.Zergling,      Latest_NinePool_ZerglingCompleteBy),
      new FingerprintArrivesBy(Zerg.Zergling,       Latest_NinePool_ZerglingArrivesBy))))
