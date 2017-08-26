package Information.StrategyDetection.ZergStrategies

import Information.StrategyDetection.Generic.{FingerprintAnd, FingerprintCompleteBy, FingerprintNot, GameTime}
import ProxyBwapi.Races.Zerg

class Fingerprint12Pool extends FingerprintAnd(
  new FingerprintNot(new Fingerprint4Pool),
  new FingerprintNot(new Fingerprint9Pool),
  new FingerprintNot(new FingerprintOverpool),
  new FingerprintCompleteBy(Zerg.SpawningPool, GameTime(2, 40))
)
