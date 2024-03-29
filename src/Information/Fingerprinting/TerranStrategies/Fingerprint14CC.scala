package Information.Fingerprinting.TerranStrategies

import Information.Fingerprinting.Generic.{FingerprintAnd, FingerprintCompleteBy, FingerprintNot}
import Lifecycle.With
import ProxyBwapi.Races.Terran
import Utilities.Time.GameTime

class Fingerprint14CC extends FingerprintAnd(
  new FingerprintNot(With.fingerprints.oneRaxFE),
  new FingerprintCompleteBy(Terran.CommandCenter,  GameTime(4, 0), 2)) {
  override def sticky: Boolean = With.frame > GameTime(4, 0)()
}
