package Information.Fingerprinting.ZergStrategies

import Information.Fingerprinting.Generic.{FingerprintAnd, FingerprintUpgradeBy}
import Lifecycle.With
import ProxyBwapi.Races.Zerg
import Utilities.Time.GameTime

class Fingerprint3HatchSpeedling extends FingerprintAnd(
  With.fingerprints.threeHatchGas,
  new FingerprintUpgradeBy(Zerg.ZerglingSpeed, GameTime(5, 0))) {

  override protected val sticky: Boolean = true
}
