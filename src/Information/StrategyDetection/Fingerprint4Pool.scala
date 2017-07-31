package Information.StrategyDetection

import ProxyBwapi.Races.Zerg

object Fingerprint4Pool extends FingerprintOr(
  FingerprintArrivesBy(Zerg.Zergling,       GameTime(2, 30)),
  FingerprintCompleteBy(Zerg.SpawningPool,  GameTime(1, 40))
)
