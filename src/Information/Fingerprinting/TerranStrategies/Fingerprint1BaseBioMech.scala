package Information.Fingerprinting.TerranStrategies

import Information.Fingerprinting.Generic.{FingerprintAnd, FingerprintOr}
import Lifecycle.With
import Utilities.Time.Minutes

class Fingerprint1BaseBioMech extends FingerprintAnd(
  new FingerprintOr(
    With.fingerprints.bio,
    With.fingerprints.twoRaxAcad),
  new FingerprintOr(
    With.fingerprints.oneFac,
    With.fingerprints.twoFac,
    With.fingerprints.threeFac)) {
  override val sticky: Boolean = true
  override val lockAfter: Int = Minutes(6)()
}
