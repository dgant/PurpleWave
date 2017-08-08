package Information.StrategyDetection.ZergStrategies

import Information.StrategyDetection.Generic.{FingerprintArrivesBy, FingerprintCompleteBy, FingerprintOr, GameTime}
import ProxyBwapi.Races.Zerg

object Fingerprint4Pool extends FingerprintOr(
  new FingerprintArrivesBy(Zerg.Zergling,       GameTime(2, 50)),
  FingerprintCompleteBy(Zerg.SpawningPool,  GameTime(1, 40))
)
