package Information.Fingerprinting.ZergStrategies

import Information.Fingerprinting.Generic.{FingerprintArrivesBy, FingerprintCompleteBy, FingerprintOr}
import ProxyBwapi.Races.Zerg
import Utilities.Time.GameTime

class Fingerprint4Pool extends FingerprintOr(
  new FingerprintArrivesBy(Zerg.Zergling,       GameTime(2, 42)),
  new FingerprintCompleteBy(Zerg.Zergling,      GameTime(2, 12)),
  new FingerprintCompleteBy(Zerg.SpawningPool,  GameTime(1, 40)))