package Information.Fingerprinting.Generic

import Information.Fingerprinting.Fingerprint
import Lifecycle.With
import Planning.UnitMatchers.UnitMatcher
import Utilities.GameTime

class FingerprintExistsBy(unitMatcher: UnitMatcher, gameTime: GameTime, quantity: Int = 1) extends Fingerprint {
  override def investigate: Boolean = With.frame < gameTime() && With.units.countEverP(unitMatcher) >= quantity
  override val sticky: Boolean = true
}
