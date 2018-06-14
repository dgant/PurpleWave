package Information.Intelligenze.Fingerprinting.Generic

import Planning.UnitMatchers.UnitMatcher

class FingerprintCompleteBy(
  unitMatcher : UnitMatcher,
  gameTime    : GameTime,
  quantity    : Int = 1)
    extends AbstractFingerprintQuantityBy(unitMatcher, gameTime) {
  
  override def investigate: Boolean = observed >= quantity
}
