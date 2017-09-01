package Information.StrategyDetection.ZergStrategies

import Information.StrategyDetection.Generic.{FingerprintAnd, FingerprintCompleteBy, GameTime}
import ProxyBwapi.Races.Zerg

class Fingerprint10Hatch9Pool extends FingerprintAnd(
  new FingerprintCompleteBy(Zerg.Hatchery, GameTime(2, 50)),
  new FingerprintCompleteBy(Zerg.SpawningPool, GameTime(2, 50))
)
