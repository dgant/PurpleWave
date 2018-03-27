package Information.Intelligenze.Fingerprinting.ZergStrategies

import Information.Intelligenze.Fingerprinting.Generic.{FingerprintAnd, FingerprintCompleteBy, GameTime}
import ProxyBwapi.Races.Zerg

class Fingerprint10Hatch9Pool extends FingerprintAnd(
  new FingerprintCompleteBy(Zerg.Hatchery, GameTime(2, 50), 2),
  new FingerprintCompleteBy(Zerg.SpawningPool, GameTime(2, 50))
)
