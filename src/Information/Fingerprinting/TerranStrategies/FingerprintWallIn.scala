package Information.Fingerprinting.TerranStrategies

import Information.Fingerprinting.Fingerprint
import Lifecycle.With
import Utilities.Time.GameTime

class FingerprintWallIn extends Fingerprint {
  override protected def investigate: Boolean = With.frame < GameTime(3, 0)() && With.geography.zones.exists(z => z.walledIn && ! z.metro.exists(_.bases.exists(_.owner.isUs)))
  override protected def sticky: Boolean = true
}