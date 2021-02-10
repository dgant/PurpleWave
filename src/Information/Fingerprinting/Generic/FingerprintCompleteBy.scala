package Information.Fingerprinting.Generic

import Lifecycle.With
import Planning.UnitMatchers.Matcher
import Utilities.GameTime

class FingerprintCompleteBy(
                             unitMatcher : Matcher,
                             gameTime    : GameTime,
                             quantity    : Int = 1)
    extends AbstractFingerprintQuantityBy(unitMatcher, gameTime) {
  
  override def investigate: Boolean = With.frame < gameTime() && observed >= quantity
}
