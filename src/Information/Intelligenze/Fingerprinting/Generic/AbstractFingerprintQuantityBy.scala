package Information.Intelligenze.Fingerprinting.Generic

import Information.Intelligenze.Fingerprinting.Fingerprint
import Lifecycle.With
import Planning.Composition.UnitMatchers.UnitMatcher

abstract class AbstractFingerprintQuantityBy(
  unitMatcher : UnitMatcher,
  gameTime  : GameTime)
    extends Fingerprint {

  def observed: Int = {
    With.units.countEnemy(u => u.is(unitMatcher) && u.completionFrame <= gameTime.frames)
  }
}
