package Information.Fingerprinting.ZergStrategies

import Information.Fingerprinting.Generic.{FingerprintArrivesBy, FingerprintCompleteBy, FingerprintOr}
import ProxyBwapi.Races.Zerg
import Utilities.GameTime

class Fingerprint4Pool extends FingerprintOr(
  new FingerprintArrivesBy(Zerg.Zergling,       GameTime(2, 45)),
  new FingerprintCompleteBy(Zerg.Zergling,      GameTime(2, 18)), // 9 Pool finishes 2:02 at earliest
  new FingerprintCompleteBy(Zerg.SpawningPool,  GameTime(1, 40))) {
  
  override val sticky = true
}
