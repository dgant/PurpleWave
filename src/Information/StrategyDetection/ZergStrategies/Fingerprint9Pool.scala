package Information.StrategyDetection.ZergStrategies

import Information.StrategyDetection.Generic.{FingerprintAnd, FingerprintCompleteBy, FingerprintNot, GameTime}
import ProxyBwapi.Races.Zerg

class Fingerprint9Pool extends FingerprintAnd(
  new FingerprintNot(new Fingerprint4Pool),
  new FingerprintCompleteBy(Zerg.SpawningPool, GameTime(2, 5))
)
