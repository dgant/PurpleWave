package Information.Fingerprinting.ZergStrategies

import Information.Fingerprinting.Generic.{FingerprintAnd, FingerprintCompleteBy, FingerprintNot}
import Lifecycle.With
import ProxyBwapi.Races.Zerg
import Utilities.GameTime

class Fingerprint12Pool extends FingerprintAnd(
  new FingerprintNot(With.fingerprints.fourPool),
  new FingerprintNot(With.fingerprints.ninePool),
  new FingerprintNot(With.fingerprints.overpool),
  new FingerprintNot(With.fingerprints.tenHatch),
  new FingerprintCompleteBy(Zerg.SpawningPool, GameTime(2, 40))
)
