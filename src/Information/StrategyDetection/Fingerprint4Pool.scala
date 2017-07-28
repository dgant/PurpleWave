package Information.StrategyDetection

import ProxyBwapi.Races.Zerg

object Fingerprint4Pool extends FingerprintOr(
  FingerprintArrivesBy(Zerg.Zergling,       GameTime(2, 30)),
  FingerprintArrivesBy(Zerg.Drone,          GameTime(2, 15)),
  FingerprintCompleteBy(Zerg.SpawningPool,  GameTime(1, 40))
)
