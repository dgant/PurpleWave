package Information.StrategyDetection.ZergStrategies

import Information.StrategyDetection._
import ProxyBwapi.Races.Zerg

object FingerprintOverpool extends FingerprintAnd(
  FingerprintNot(Fingerprint4Pool),
  FingerprintNot(Fingerprint9Pool),
  FingerprintCompleteBy(Zerg.SpawningPool, GameTime(2, 20))
)
