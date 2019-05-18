package Information.Intelligenze.Fingerprinting.ZergStrategies

import Information.Intelligenze.Fingerprinting.Generic._
import Lifecycle.With
import ProxyBwapi.Races.Zerg

class Fingerprint9Pool extends FingerprintOr(
  With.fingerprints.ninePoolGas,
  new FingerprintAnd(
    new FingerprintNot(With.fingerprints.fourPool),
    new FingerprintOr(
      new FingerprintArrivesBy(Zerg.Zergling,       GameTime(3, 15)),
      new FingerprintCompleteBy(Zerg.SpawningPool,  GameTime(1, 40)))))
