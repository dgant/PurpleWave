package Information.Fingerprinting.TerranStrategies

import Information.Fingerprinting.Generic.{FingerprintAnd, FingerprintCompleteBy, FingerprintNot, GameTime}
import Lifecycle.With
import ProxyBwapi.Races.Terran

class Fingerprint14CC extends FingerprintAnd(
  new FingerprintNot(With.fingerprints.oneRaxFE),
  new FingerprintCompleteBy(Terran.CommandCenter,  GameTime(4, 0), 2)) {
  override val sticky = true
}
