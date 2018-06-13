package Information.Intelligenze.Fingerprinting.ZergStrategies

import Information.Intelligenze.Fingerprinting.Generic.{FingerprintAnd, FingerprintCompleteBy, FingerprintNot, GameTime}
import Lifecycle.With
import ProxyBwapi.Races.Zerg

class Fingerprint9Pool extends FingerprintAnd(
  new FingerprintNot(With.fingerprints.fourPool),
  new FingerprintCompleteBy(Zerg.SpawningPool, GameTime(2, 5))
)
