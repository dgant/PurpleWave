package Information.StrategyDetection.ZergStrategies

import Information.StrategyDetection.Generic.{FingerprintArrivesBy, FingerprintCompleteBy, FingerprintOr, GameTime}
import Information.StrategyDetection._
import ProxyBwapi.Races.Zerg

object Fingerprint4Pool extends FingerprintOr(
  FingerprintArrivesBy(Zerg.Zergling,       GameTime(2, 30)),
  FingerprintCompleteBy(Zerg.SpawningPool,  GameTime(1, 40))
)
