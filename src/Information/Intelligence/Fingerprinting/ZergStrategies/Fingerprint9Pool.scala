package Information.Intelligence.Fingerprinting.ZergStrategies

import Information.Intelligence.Fingerprinting.Generic.{FingerprintAnd, FingerprintCompleteBy, FingerprintNot, GameTime}
import Lifecycle.With
import ProxyBwapi.Races.Zerg

class Fingerprint9Pool extends FingerprintAnd(
  new FingerprintNot(With.intelligence.fingerprints.fingerprint4Pool),
  new FingerprintCompleteBy(Zerg.SpawningPool, GameTime(2, 5))
)
