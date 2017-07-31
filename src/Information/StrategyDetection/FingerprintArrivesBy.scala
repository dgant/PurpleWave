package Information.StrategyDetection

import Lifecycle.With
import ProxyBwapi.UnitClass.UnitClass

case class FingerprintArrivesBy(
  unitClass : UnitClass,
  gameTime  : GameTime,
  quantity  : Int = 1) extends Fingerprint {
  
  override def matches: Boolean = {
    With.units.enemy.count(u =>
      u.is(unitClass)
      && With.frame + u.framesToTravelTo(With.geography.home.pixelCenter) < gameTime.frames) >= quantity
  }
}
