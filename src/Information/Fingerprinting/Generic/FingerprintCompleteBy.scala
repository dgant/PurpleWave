package Information.Fingerprinting.Generic

import Lifecycle.With
import Planning.UnitMatchers.UnitMatcher
import Utilities.GameTime

class FingerprintCompleteBy(
  unitMatcher : UnitMatcher,
  gameTime    : GameTime,
  quantity    : Int = 1)
    extends AbstractFingerprintQuantityBy(unitMatcher, gameTime) {
  
  override def investigate: Boolean = With.frame < gameTime() && observed >= quantity
}
