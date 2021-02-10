package Information.Fingerprinting.Generic

import Information.Fingerprinting.Fingerprint
import Lifecycle.With
import Planning.UnitMatchers.Matcher
import Utilities.GameTime

abstract class AbstractFingerprintQuantityBy(
                                              unitMatcher : Matcher,
                                              gameTime    : GameTime)
    extends Fingerprint {

  def observed: Int = {
    With.units.countEverP(u => u.isEnemy
      && (u.is(unitMatcher) && u.completionFrame <= gameTime.frames))
      // TODO: Maybe use completion/arrival time of produced units
  }

  override def sticky: Boolean = true
}
