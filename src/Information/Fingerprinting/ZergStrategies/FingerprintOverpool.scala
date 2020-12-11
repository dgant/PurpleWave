package Information.Fingerprinting.ZergStrategies

import Information.Fingerprinting.Generic._
import Lifecycle.With
import ProxyBwapi.Races.Zerg
import Utilities.GameTime

class FingerprintOverpool extends FingerprintAnd(
  new FingerprintNot(With.fingerprints.fourPool),
  new FingerprintNot(With.fingerprints.ninePool),
  new FingerprintOr(
    new FingerprintArrivesBy(Zerg.Zergling,       GameTime(3, 25)),
    new FingerprintCompleteBy(Zerg.Zergling,      GameTime(2, 38)),
    new FingerprintCompleteBy(Zerg.SpawningPool,  GameTime(2, 20)))
)
