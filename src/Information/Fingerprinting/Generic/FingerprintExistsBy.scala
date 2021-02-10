package Information.Fingerprinting.Generic

import Information.Fingerprinting.Fingerprint
import Lifecycle.With
import Planning.UnitMatchers.Matcher
import Utilities.GameTime

class FingerprintExistsBy(unitMatcher: Matcher, gameTime: GameTime, quantity: Int = 1) extends Fingerprint {
  override def investigate: Boolean = With.frame < gameTime() && With.units.countEverP(_.is(unitMatcher)) >= quantity
  override val sticky: Boolean = true
}
