package Information.Fingerprinting.ZergStrategies

import Information.Fingerprinting.Generic._
import ProxyBwapi.Races.Zerg
import Utilities.GameTime

class Fingerprint9PoolGas extends FingerprintOr(
  new FingerprintCompleteBy(Zerg.Extractor, GameTime(1, 50)),
  new FingerprintUpgradeBy(Zerg.ZerglingSpeed, GameTime(3, 30)),
  new FingerprintExistsBy(Zerg.Lair, GameTime(3, 30)))
