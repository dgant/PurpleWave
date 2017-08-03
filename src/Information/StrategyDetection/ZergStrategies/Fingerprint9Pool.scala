package Information.StrategyDetection.ZergStrategies

import Information.StrategyDetection.Generic.{FingerprintAnd, FingerprintCompleteBy, FingerprintNot, GameTime}
import Information.StrategyDetection._
import ProxyBwapi.Races.Zerg

object Fingerprint9Pool extends FingerprintAnd(
  FingerprintNot(Fingerprint4Pool),
  FingerprintCompleteBy(Zerg.SpawningPool, GameTime(2, 0))
)
