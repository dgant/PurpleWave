package Information.Fingerprinting.ZergStrategies

import Information.Fingerprinting.Generic._
import Information.Fingerprinting.ZergStrategies.ZergTimings.{Latest_Overpool_PoolCompleteBy, Latest_Overpool_ZerglingArrivesBy, Latest_Overpool_ZerglingCompleteBy}
import Lifecycle.With
import ProxyBwapi.Races.Zerg

class FingerprintOverpool extends FingerprintAnd(
  new FingerprintNot(With.fingerprints.fourPool),
  new FingerprintNot(With.fingerprints.ninePool),
  new FingerprintOr(
    new FingerprintCompleteBy(Zerg.SpawningPool,  Latest_Overpool_PoolCompleteBy),
    new FingerprintCompleteBy(Zerg.Zergling,      Latest_Overpool_ZerglingCompleteBy),
    new FingerprintArrivesBy(Zerg.Zergling,       Latest_Overpool_ZerglingArrivesBy)))
