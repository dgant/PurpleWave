package Information.StrategyDetection.ZergStrategies

import Information.StrategyDetection.Generic.{FingerprintAnd, FingerprintCompleteBy, FingerprintNot, GameTime}
import ProxyBwapi.Races.Zerg

class FingerprintOverpool extends FingerprintAnd(
  new FingerprintNot(new Fingerprint4Pool),
  new FingerprintNot(new Fingerprint9Pool),
  new FingerprintCompleteBy(Zerg.SpawningPool, GameTime(2, 20))
)
