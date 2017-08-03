package Information.StrategyDetection.Generic

import Information.StrategyDetection.Fingerprint
import Lifecycle.With
import ProxyBwapi.UnitClass.UnitClass

abstract class AbstractFingerprintQuantityBy(
  unitClass : UnitClass,
  gameTime  : GameTime)
    extends Fingerprint {

  def observed: Int = {
    With.units.enemy.count(u =>
      u.is(unitClass)
        && u.completionFrame <= gameTime.frames)
  }
}
