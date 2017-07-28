package Information.StrategyDetection

import Lifecycle.With
import ProxyBwapi.UnitClass.UnitClass

case class FingerprintCompleteBy(
  unitClass : UnitClass,
  gameTime  : GameTime,
  quantity  : Int = 1) extends Fingerprint {
  
  override def matches: Boolean = {
    With.units.enemy.count(u =>
      u.is(unitClass)
      && u.completionFrame <= gameTime.frames) >= quantity
  }
}
