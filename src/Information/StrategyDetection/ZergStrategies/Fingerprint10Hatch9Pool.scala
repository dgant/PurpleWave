package Information.StrategyDetection.ZergStrategies

import Information.StrategyDetection._
import ProxyBwapi.Races.Zerg

object Fingerprint10Hatch9Pool extends FingerprintAnd(
  FingerprintCompleteBy(Zerg.Hatchery, GameTime(2, 55)),
  FingerprintCompleteBy(Zerg.SpawningPool, GameTime(2, 55))
)
