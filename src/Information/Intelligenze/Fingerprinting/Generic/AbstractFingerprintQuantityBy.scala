package Information.Intelligenze.Fingerprinting.Generic

import Information.Intelligenze.Fingerprinting.Fingerprint
import Lifecycle.With
import ProxyBwapi.UnitClass.UnitClass

abstract class AbstractFingerprintQuantityBy(
  unitClass : UnitClass,
  gameTime  : GameTime)
    extends Fingerprint {

  def observed: Int = {
    With.units.countEnemy(u =>
      u.is(unitClass)
        && u.completionFrame <= gameTime.frames)
  }
}
