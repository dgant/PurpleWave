package Information.Intelligenze.Fingerprinting.ZergStrategies

import Information.Intelligenze.Fingerprinting.Generic._
import ProxyBwapi.Races.Zerg

class Fingerprint9PoolGas extends FingerprintOr(
  new FingerprintCompleteBy(Zerg.Extractor, GameTime(1, 50)),
  new FingerprintUpgradeBy(Zerg.ZerglingSpeed, GameTime(3, 30)),
  new FingerprintExistsBy(Zerg.Lair, GameTime(3, 30)))
