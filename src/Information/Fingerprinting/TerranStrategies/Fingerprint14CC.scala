package Information.Fingerprinting.TerranStrategies

import Information.Fingerprinting.Generic.{FingerprintAnd, FingerprintCompleteBy, FingerprintNot}
import Lifecycle.With
import ProxyBwapi.Races.Terran
import Utilities.GameTime

class Fingerprint14CC extends FingerprintAnd(
  new FingerprintNot(With.fingerprints.oneRaxFE),
  new FingerprintCompleteBy(Terran.CommandCenter,  GameTime(4, 0), 2)) {
  override val sticky = true
}
