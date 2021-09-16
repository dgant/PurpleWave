package Information.Fingerprinting.Generic

import Information.Fingerprinting.Fingerprint
import Lifecycle.With
import Planning.UnitMatchers.UnitMatcher
import Utilities.Time.GameTime

class FingerprintExistsBy(unitMatcher: UnitMatcher, gameTime: GameTime, quantity: Int = 1) extends Fingerprint {
  override def investigate: Boolean = With.frame < gameTime() && observed >= quantity
  protected def observed: Int = With.units.countEverP(unitMatcher)
  override def reason: String = f"$observed $unitMatcher exist by $gameTime"
  override val sticky: Boolean = true
}
