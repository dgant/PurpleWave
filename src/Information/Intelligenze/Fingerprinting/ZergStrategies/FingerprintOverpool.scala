package Information.Intelligenze.Fingerprinting.ZergStrategies

import Information.Intelligenze.Fingerprinting.Generic.{FingerprintAnd, FingerprintCompleteBy, FingerprintNot, GameTime}
import Lifecycle.With
import ProxyBwapi.Races.Zerg

class FingerprintOverpool extends FingerprintAnd(
  new FingerprintNot(With.intelligence.fingerprints.fourPool),
  new FingerprintNot(With.intelligence.fingerprints.ninePool),
  new FingerprintCompleteBy(Zerg.SpawningPool, GameTime(2, 20))
)
